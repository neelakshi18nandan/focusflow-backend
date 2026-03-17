package com.focusflow.service;

import com.focusflow.model.Dto.*;
import com.focusflow.model.Session;
import com.focusflow.model.StudyLog;
import com.focusflow.model.User;
import com.focusflow.repository.SessionRepository;
import com.focusflow.repository.StudyLogRepository;
import com.focusflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired private SessionRepository sessionRepository;
    @Autowired private StudyLogRepository studyLogRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Called when the user's timer ends OR when "End the Day" is clicked.
     *
     * 1. Saves the individual session row.
     * 2. Upserts (creates or updates) the daily study_log row for that user+date.
     */
    @Transactional
    public SessionResponse saveSession(SessionRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        // 1 — Save the individual session
        Session session = new Session();
        session.setUser(user);
        session.setPlannedSec(req.getPlannedSec());
        session.setDurationSec(req.getDurationSec());
        session.setDate(date);
        Session saved = sessionRepository.save(session);

        // 2 — Upsert the daily study log
        upsertStudyLog(user, date);

        return new SessionResponse(saved.getId(), saved.getPlannedSec(),
                                   saved.getDurationSec(), saved.getDate());
    }

    /**
     * Creates or updates the study_log row for (user, date).
     * Recalculates totals from all sessions on that day.
     */
    private void upsertStudyLog(User user, LocalDate date) {
        // Sum actual seconds studied today
        int totalActual  = sessionRepository.sumDurationByUserAndDate(user.getId(), date);
        // Sum planned seconds today
        List<Session> daySessions = sessionRepository.findByUserIdAndDate(user.getId(), date);
        int totalPlanned = daySessions.stream().mapToInt(Session::getPlannedSec).sum();
        int count        = daySessions.size();

        StudyLog log = studyLogRepository.findByUserIdAndDate(user.getId(), date)
                .orElse(new StudyLog()); // create new if no log exists for today

        log.setUser(user);
        log.setDate(date);
        log.setPlannedSec(totalPlanned);
        log.setActualSec(totalActual);
        log.setSessionCount(count);

        studyLogRepository.save(log);
    }

    /** Get all sessions for a user (e.g. for a history view) */
    public List<SessionResponse> getSessionsByUser(Long userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .map(s -> new SessionResponse(s.getId(), s.getPlannedSec(),
                                              s.getDurationSec(), s.getDate()))
                .collect(Collectors.toList());
    }
}

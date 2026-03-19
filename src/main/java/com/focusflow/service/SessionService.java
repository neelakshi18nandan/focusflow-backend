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
     * Called each time a pomodoro timer cycle completes.
     * Saves the individual session row and upserts the daily study_log.
     */
    @Transactional
    public SessionResponse saveSession(SessionRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));

        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now();

        // Save the individual session
        Session session = new Session();
        session.setUser(user);
        session.setPlannedSec(req.getPlannedSec());
        session.setDurationSec(req.getDurationSec());
        session.setDate(date);
        Session saved = sessionRepository.save(session);

        // Upsert the daily study log
        upsertStudyLog(user, date);

        return new SessionResponse(saved.getId(), saved.getPlannedSec(),
                                   saved.getDurationSec(), saved.getDate());
    }

    /**
     * Called when "End the Day" is clicked OR 24hr scheduler triggers.
     * 1. Upserts the daily study_log with final totals.
     * 2. Deletes all session rows for that user+date (cleanup).
     */
    @Transactional
    public void endDay(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        List<Session> daySessions = sessionRepository.findByUserIdAndDate(userId, date);

        if (!daySessions.isEmpty()) {
            // Calculate totals from all sessions
            int totalActual  = daySessions.stream().mapToInt(Session::getDurationSec).sum();
            int totalPlanned = daySessions.stream().mapToInt(Session::getPlannedSec).max().orElse(0);
            int count        = daySessions.size();

            // Upsert study_log
            StudyLog log = studyLogRepository.findByUserIdAndDate(userId, date)
                    .orElse(new StudyLog());
            log.setUser(user);
            log.setDate(date);
            log.setPlannedSec(totalPlanned);
            log.setActualSec(totalActual);
            log.setSessionCount(count);
            studyLogRepository.save(log);

            // Delete all sessions for this day — data is now in study_log
            sessionRepository.deleteAll(daySessions);
        }
    }

    /**
     * Creates or updates the study_log row for (user, date).
     * Recalculates totals from all sessions on that day.
     */
    private void upsertStudyLog(User user, LocalDate date) {
        int totalActual  = sessionRepository.sumDurationByUserAndDate(user.getId(), date);
        List<Session> daySessions = sessionRepository.findByUserIdAndDate(user.getId(), date);
        int totalPlanned = daySessions.stream().mapToInt(Session::getPlannedSec).max().orElse(0);
        int count        = daySessions.size();

        StudyLog log = studyLogRepository.findByUserIdAndDate(user.getId(), date)
                .orElse(new StudyLog());

        log.setUser(user);
        log.setDate(date);
        log.setPlannedSec(totalPlanned);
        log.setActualSec(totalActual);
        log.setSessionCount(count);

        studyLogRepository.save(log);
    }

    /** Get all sessions for a user */
    public List<SessionResponse> getSessionsByUser(Long userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .map(s -> new SessionResponse(s.getId(), s.getPlannedSec(),
                                              s.getDurationSec(), s.getDate()))
                .collect(Collectors.toList());
    }
}
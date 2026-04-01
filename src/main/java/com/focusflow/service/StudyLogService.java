package com.focusflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focusflow.model.Dto.*;
import com.focusflow.model.Dto.AnalyticsSummary;
import com.focusflow.model.Dto.StudyLogResponse;
import com.focusflow.model.Sessions;
import com.focusflow.model.StudyLog;
import com.focusflow.model.User;
import com.focusflow.repository.SessionsRepository;
import com.focusflow.repository.StudyLogRepository;
import com.focusflow.repository.UserRepository;

@Service
public class StudyLogService {

    @Autowired private StudyLogRepository studyLogRepository;
    @Autowired private SessionsRepository sessionsRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Called after EVERY pomodoro completes.
     * 1. Inserts a new row into sessions (individual pomodoro record)
     * 2. Upserts study_log for today (daily aggregate)
     */
    @Transactional
    public StudyLogResponse recordSession(Long userId, int durationSec, Integer plannedSec) {
        LocalDate today = LocalDate.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ── Step 1: Insert into sessions (one row per pomodoro) ──
        Sessions session = new Sessions();
        session.setUser(user);
        session.setDate(today);
        session.setActualSec(durationSec);
        session.setPlannedSec(plannedSec);
        session.setLoggedAt(LocalDateTime.now());
        sessionsRepository.save(session);

        // ── Step 2: Upsert study_log (daily aggregate) ──
        Optional<StudyLog> existing = studyLogRepository.findByUser_IdAndDate(userId, today);  // FIXED
        StudyLog log;

        // Count sessions for today to update session_count
        int todaySessionCount = sessionsRepository.findByUser_IdAndDate(userId, today).size();  // FIXED

        if (existing.isPresent()) {
            log = existing.get();
            log.setActualSec(log.getActualSec() + durationSec);
            log.setSessionCount(todaySessionCount);
            if (plannedSec != null) log.setPlannedSec(plannedSec);
        } else {
            log = new StudyLog();
            log.setUser(user);
            log.setDate(today);
            log.setActualSec(durationSec);
            log.setSessionCount(todaySessionCount);
            log.setPlannedSec(plannedSec);
        }

        return toResponse(studyLogRepository.save(log));
    }

    /**
     * Full analytics summary — used by analysis page.
     */
    public AnalyticsSummary getAnalytics(Long userId) {
        List<StudyLog> logs = studyLogRepository.findByUser_IdOrderByDateDesc(userId);  // FIXED

        List<StudyLogResponse> logResponses = logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        double totalHours    = logs.stream().mapToInt(StudyLog::getActualSec).sum() / 3600.0;
        int    totalSessions = logs.stream().mapToInt(StudyLog::getSessionCount).sum();
        double avgDaily      = logs.isEmpty() ? 0 : totalHours / logs.size();
        int    streak        = calculateStreak(logs);

        return new AnalyticsSummary(
                Math.round(totalHours * 10) / 10.0,
                totalSessions,
                streak,
                Math.round(avgDaily * 10) / 10.0,
                logResponses
        );
    }

    /**
     * Logs in a date range — for monthly charts.
     */
    public List<StudyLogResponse> getLogsInRange(Long userId, LocalDate from, LocalDate to) {
        return studyLogRepository
                .findByUser_IdAndDateBetweenOrderByDate(userId, from, to)  // FIXED
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private StudyLogResponse toResponse(StudyLog log) {
        StudyLogResponse r = new StudyLogResponse();
        r.setLogId(log.getLogId());
        r.setDate(log.getDate());
        r.setPlannedSec(log.getPlannedSec() != null ? log.getPlannedSec() : 0);
        r.setActualSec(log.getActualSec());
        r.setSessionCount(log.getSessionCount());
        r.setPlannedHours(Math.round((log.getPlannedSec() != null ? log.getPlannedSec() : 0) / 360.0) / 10.0);
        r.setActualHours(Math.round(log.getActualSec() / 360.0) / 10.0);
        return r;
    }

    private int calculateStreak(List<StudyLog> logs) {
        if (logs.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate mostRecentLogDate = logs.get(0).getDate();

        if (mostRecentLogDate.isBefore(today.minusDays(1))) return 0;

        int streak = 0;
        LocalDate expected = mostRecentLogDate;

        for (StudyLog log : logs) {
            if (log.getDate().equals(expected) && log.getSessionCount() > 0) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
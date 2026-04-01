package com.focusflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.focusflow.model.Dto.AnalyticsSummary;
import com.focusflow.model.Dto.StudyLogResponse;
import com.focusflow.model.StudyLog;
import com.focusflow.model.User;
import com.focusflow.repository.SessionsRepository;
import com.focusflow.repository.StudyLogRepository; // FIXED: was SessionRepository
import com.focusflow.repository.UserRepository;

@Service
public class StudyLogService {

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private SessionsRepository sessionsRepository; // FIXED: correct name

    @Autowired
    private UserRepository userRepository;

    // ── Record a completed pomodoro session ───────────────────────────────────

    public StudyLogResponse recordSession(Long userId, int durationSec, int plannedSec) {
        LocalDate today = LocalDate.now();

        Optional<StudyLog> existing = studyLogRepository.findByUser_IdAndDate(userId, today);

        StudyLog log;
        if (existing.isPresent()) {
            log = existing.get();
            log.setActualSec(log.getActualSec() + durationSec);
            if (plannedSec > 0 && (log.getPlannedSec() == null || plannedSec > log.getPlannedSec())) {
                log.setPlannedSec(plannedSec);
            }
        } else {
            // FIXED: StudyLog uses User object, not setUserId()
            User user = userRepository.findById(userId).orElseThrow();
            log = new StudyLog();
            log.setUser(user);
            log.setDate(today);
            log.setActualSec(durationSec);
            log.setPlannedSec(plannedSec);
        }

        // Count sessions from SessionsRepository for today
        int sessionCount = sessionsRepository.findByUser_IdAndDate(userId, today).size();
        log.setSessionCount(sessionCount);

        studyLogRepository.save(log);

        return toResponse(log);
    }

    // ── Full analytics for the analysis page ──────────────────────────────────

    public AnalyticsSummary getAnalytics(Long userId) {
        List<StudyLog> logs = studyLogRepository.findByUser_IdOrderByDateDesc(userId);

        double totalHours = logs.stream()
                .mapToInt(StudyLog::getActualSec)
                .sum() / 3600.0;

        int totalSessions = logs.stream()
                .mapToInt(StudyLog::getSessionCount)
                .sum();

        int streak = calculateStreak(logs);

        double avgDailyHours = logs.isEmpty() ? 0 : totalHours / logs.size();

        List<StudyLogResponse> logResponses = logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new AnalyticsSummary(totalHours, totalSessions, streak, avgDailyHours, logResponses);
    }

    // ── Logs in a date range ──────────────────────────────────────────────────

    public List<StudyLogResponse> getLogsInRange(Long userId, LocalDate from, LocalDate to) {
        return studyLogRepository.findByUser_IdAndDateBetweenOrderByDate(userId, from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Streak calculation ────────────────────────────────────────────────────

    private int calculateStreak(List<StudyLog> logs) {
        if (logs.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate mostRecentLogDate = logs.get(0).getDate();

        // Allow streak if last study was today OR yesterday
        if (mostRecentLogDate.isBefore(today.minusDays(1))) return 0;

        int streak = 0;
        LocalDate expected = mostRecentLogDate;

        for (StudyLog log : logs) {
            // FIXED: use actualSec > 0 (more reliable than sessionCount)
            if (log.getDate().equals(expected) && log.getActualSec() > 0) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private StudyLogResponse toResponse(StudyLog log) {
        return new StudyLogResponse(
                log.getLogId(),          // FIXED: was getId(), entity uses getLogId()
                log.getDate(),
                log.getActualSec(),
                log.getPlannedSec() != null ? log.getPlannedSec() : 0,
                log.getSessionCount()
        );
    }
}
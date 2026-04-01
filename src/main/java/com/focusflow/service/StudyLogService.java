package com.focusflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.focusflow.model.Dto.AnalyticsSummary;
import com.focusflow.model.Dto.StudyLogResponse;
import com.focusflow.model.StudyLog;
import com.focusflow.repository.StudyLogRepository;
import com.focusflow.repository.SessionRepository;

@Service
public class StudyLogService {

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private SessionRepository sessionRepository;

    // ── Record a completed pomodoro session ───────────────────────────────────

    public StudyLogResponse recordSession(Long userId, int durationSec, int plannedSec) {
        LocalDate today = LocalDate.now();

        // FIXED: findByUser_Id (was findByUserId)
        Optional<StudyLog> existing = studyLogRepository.findByUser_IdAndDate(userId, today);

        StudyLog log;
        if (existing.isPresent()) {
            log = existing.get();
            log.setActualSec(log.getActualSec() + durationSec);
            log.setSessionCount(log.getSessionCount() + 1);
            // Update plannedSec only if it's larger (user may have changed goal mid-day)
            if (plannedSec > log.getPlannedSec()) {
                log.setPlannedSec(plannedSec);
            }
        } else {
            log = new StudyLog();
            log.setUserId(userId);
            log.setDate(today);
            log.setActualSec(durationSec);
            log.setPlannedSec(plannedSec);
            log.setSessionCount(1);
        }

        // FIXED: findByUser_IdAndDate for sessions count (was findByUserIdAndDate)
        int sessionCountToday = sessionRepository.findByUser_IdAndDate(userId, today).size();
        log.setSessionCount(sessionCountToday + 1);

        studyLogRepository.save(log);

        return toResponse(log);
    }

    // ── Full analytics for the analysis page ──────────────────────────────────

    public AnalyticsSummary getAnalytics(Long userId) {
        // FIXED: findByUser_IdOrderByDateDesc (was findByUserIdOrderByDateDesc)
        List<StudyLog> logs = studyLogRepository.findByUser_IdOrderByDateDesc(userId);

        double totalHours = logs.stream()
            .mapToInt(StudyLog::getActualSec)
            .sum() / 3600.0;

        int totalSessions = logs.stream()
            .mapToInt(StudyLog::getSessionCount)
            .sum();

        int streak = calculateStreak(logs);

        double avgDailyHours = logs.isEmpty() ? 0 :
            totalHours / logs.size();

        List<StudyLogResponse> logResponses = logs.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return new AnalyticsSummary(totalHours, totalSessions, streak, avgDailyHours, logResponses);
    }

    // ── Logs in a date range ──────────────────────────────────────────────────

    public List<StudyLogResponse> getLogsInRange(Long userId, LocalDate from, LocalDate to) {
        // FIXED: findByUser_IdAndDateBetweenOrderByDate (was findByUserIdAndDateBetweenOrderByDate)
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

        // FIXED: Allow streak if last study was today OR yesterday
        // (old logic broke streak if you hadn't studied yet today)
        if (mostRecentLogDate.isBefore(today.minusDays(1))) return 0;

        int streak = 0;
        LocalDate expected = mostRecentLogDate;

        for (StudyLog log : logs) {
            // FIXED: check actualSec > 0 instead of sessionCount > 0 (more reliable)
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
            log.getId(),
            log.getDate(),
            log.getActualSec(),
            log.getPlannedSec(),
            log.getSessionCount()
        );
    }
}
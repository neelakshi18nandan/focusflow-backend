package com.focusflow.service;

import com.focusflow.model.Dto.*;
import com.focusflow.model.StudyLog;
import com.focusflow.repository.StudyLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyLogService {

    @Autowired
    private StudyLogRepository studyLogRepository;

    /**
     * Full analytics summary for a user.
     * Returns all-time stats + daily log list for the analytics page.
     */
    public AnalyticsSummary getAnalytics(Long userId) {
        List<StudyLog> logs = studyLogRepository.findByUserIdOrderByDateDesc(userId);

        // Map to response DTOs
        List<StudyLogResponse> logResponses = logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // Totals
        double totalHours   = logs.stream().mapToInt(StudyLog::getActualSec).sum() / 3600.0;
        int    totalSessions= logs.stream().mapToInt(StudyLog::getSessionCount).sum();
        double avgDaily     = logs.isEmpty() ? 0 : totalHours / logs.size();
        int    streak       = calculateStreak(logs);

        return new AnalyticsSummary(
                Math.round(totalHours * 10) / 10.0,
                totalSessions,
                streak,
                Math.round(avgDaily * 10) / 10.0,
                logResponses
        );
    }

    /**
     * Logs for a specific date range (for monthly charts).
     * e.g. GET /api/logs/{userId}?from=2026-01-01&to=2026-01-31
     */
    public List<StudyLogResponse> getLogsInRange(Long userId, LocalDate from, LocalDate to) {
        return studyLogRepository
                .findByUserIdAndDateBetweenOrderByDate(userId, from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Convert model to DTO, adding convenience hour fields */
    private StudyLogResponse toResponse(StudyLog log) {
        StudyLogResponse r = new StudyLogResponse();
        r.setLogId(log.getLogId());
        r.setDate(log.getDate());
        r.setPlannedSec(log.getPlannedSec());
        r.setActualSec(log.getActualSec());
        r.setSessionCount(log.getSessionCount());
        r.setPlannedHours(Math.round(log.getPlannedSec() / 360.0) / 10.0);
        r.setActualHours(Math.round(log.getActualSec() / 360.0) / 10.0);
        return r;
    }

    /**
     * Calculate current streak (consecutive days with at least 1 session).
     * Logs are sorted newest → oldest.
     */
    private int calculateStreak(List<StudyLog> logs) {
        if (logs.isEmpty()) return 0;

        int streak = 0;
        LocalDate expected = LocalDate.now();

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

package com.focusflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.focusflow.model.Dto.*;
import com.focusflow.model.Dto.AnalyticsSummary;
import com.focusflow.model.Dto.StudyLogResponse;
import com.focusflow.model.StudyLog;
import com.focusflow.model.User;
import com.focusflow.repository.StudyLogRepository;
import com.focusflow.repository.UserRepository;

@Service
public class StudyLogService {

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Called after EVERY pomodoro session completes.
     * Upserts today's study_log row — adds durationSec and increments session_count.
     */
    public StudyLogResponse recordSession(Long userId, int durationSec, Integer plannedSec) {
        LocalDate today = LocalDate.now();

        Optional<StudyLog> existing = studyLogRepository.findByUserIdAndDate(userId, today);
        StudyLog log;

        if (existing.isPresent()) {
            log = existing.get();
            log.setActualSec(log.getActualSec() + durationSec);
            log.setSessionCount(log.getSessionCount() + 1);
            if (plannedSec != null) log.setPlannedSec(plannedSec);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log = new StudyLog();
            log.setUser(user);
            log.setDate(today);
            log.setActualSec(durationSec);
            log.setSessionCount(1);
            log.setPlannedSec(plannedSec);
        }

        return toResponse(studyLogRepository.save(log));
    }

    /**
     * Full analytics summary for a user — used by analysis page.
     */
    public AnalyticsSummary getAnalytics(Long userId) {
        List<StudyLog> logs = studyLogRepository.findByUserIdOrderByDateDesc(userId);

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
                .findByUserIdAndDateBetweenOrderByDate(userId, from, to)
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

        // Start from today OR yesterday — whichever has a log entry first
        // This way streak isn't broken just because today's session hasn't happened yet
        LocalDate today = LocalDate.now();
        LocalDate mostRecentLogDate = logs.get(0).getDate();

        // If last log is older than yesterday, streak is broken
        if (mostRecentLogDate.isBefore(today.minusDays(1))) return 0;

        // Start counting from the most recent log date
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
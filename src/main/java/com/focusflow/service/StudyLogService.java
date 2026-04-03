package com.focusflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private SessionsRepository sessionsRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Called every time a pomodoro completes or timer is reset.
     * 1) Saves individual log entry into sessions table
     * 2) Updates (upserts) daily total in study_log table
     */
    public StudyLogResponse recordSession(Long userId, int durationSec, int plannedSec, int totalPlannedSec) {
        LocalDate today = LocalDate.now();
        User user = userRepository.findById(userId).orElseThrow();

        // Step 1: Save individual log into sessions table
        Sessions log = new Sessions();
        log.setUser(user);
        log.setDate(today);
        log.setActualSec(durationSec);
        log.setDurationSec(plannedSec);
        log.setLoggedAt(java.time.LocalDateTime.now());
        sessionsRepository.save(log);

        // Step 2: Upsert daily total in study_log
        Optional<StudyLog> existing = studyLogRepository.findByUser_IdAndDate(userId, today);
        StudyLog dailyTotal;

        if (existing.isPresent()) {
            dailyTotal = existing.get();
            dailyTotal.setActualSec(dailyTotal.getActualSec() + durationSec);
            if (totalPlannedSec > 0) {
                dailyTotal.setPlannedSec(totalPlannedSec);
            }
        } else {
            dailyTotal = new StudyLog();
            dailyTotal.setUser(user);
            dailyTotal.setDate(today);
            dailyTotal.setActualSec(durationSec);
            dailyTotal.setPlannedSec(totalPlannedSec);
        }
        // session_count = number of individual logs today
        int logCount = sessionsRepository.findByUser_IdAndDate(userId, today).size();
        dailyTotal.setSessionCount(logCount);

        studyLogRepository.save(dailyTotal);

        return toResponse(dailyTotal);
    }

    /**
     * Full analytics for the analysis page.
     * Reads from study_log (daily totals).
     */
    public AnalyticsSummary getAnalytics(Long userId) {
        List<StudyLog> dailyTotals = studyLogRepository.findByUser_IdOrderByDateDesc(userId);

        double totalHours = dailyTotals.stream()
                .mapToInt(StudyLog::getActualSec)
                .sum() / 3600.0;

        // totalSessions = total number of individual logs across all days
        int totalSessions = dailyTotals.stream()
                .mapToInt(StudyLog::getSessionCount)
                .sum();

        int streak = calculateStreak(dailyTotals);

        double avgDailyHours = dailyTotals.isEmpty() ? 0 : totalHours / dailyTotals.size();

        List<StudyLogResponse> logResponses = dailyTotals.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new AnalyticsSummary(totalHours, totalSessions, streak, avgDailyHours, logResponses);
    }

    /**
     * Logs in a date range for monthly charts.
     */
    public List<StudyLogResponse> getLogsInRange(Long userId, LocalDate from, LocalDate to) {
        return studyLogRepository.findByUser_IdAndDateBetweenOrderByDate(userId, from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Streak = consecutive days with actualSec > 0 in study_log.
     */
    private int calculateStreak(List<StudyLog> logs) {
        if (logs.isEmpty())
            return 0;

        LocalDate today = LocalDate.now();
        LocalDate mostRecentLogDate = logs.get(0).getDate();

        if (mostRecentLogDate.isBefore(today.minusDays(1)))
            return 0;

        int streak = 0;
        LocalDate expected = mostRecentLogDate;

        for (StudyLog log : logs) {
            if (log.getDate().equals(expected) && log.getActualSec() > 0) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private StudyLogResponse toResponse(StudyLog log) {
        return new StudyLogResponse(
                log.getLogId(),
                log.getDate(),
                log.getActualSec(),
                log.getPlannedSec() != null ? log.getPlannedSec() : 0,
                log.getSessionCount());
    }
}
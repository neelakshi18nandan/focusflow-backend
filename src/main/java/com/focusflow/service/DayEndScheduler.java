package com.focusflow.service;

import com.focusflow.model.Session;
import com.focusflow.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Automatically ends the day for any user who has open sessions
 * from YESTERDAY (i.e. they never clicked "End the Day").
 *
 * Runs every day at midnight (00:01).
 */
@Component
public class DayEndScheduler {

    @Autowired private SessionRepository sessionRepository;
    @Autowired private SessionService sessionService;

    /**
     * Runs at 00:01 every day.
     * Finds all sessions from yesterday that were never closed (no study_log entry).
     * Calls endDay() for each affected user to save their study_log and clean up sessions.
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void autoEndPreviousDays() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Find all sessions from yesterday
        List<Session> orphanedSessions = sessionRepository.findByDate(yesterday);

        if (orphanedSessions.isEmpty()) return;

        // Get unique user IDs with sessions from yesterday
        List<Long> userIds = orphanedSessions.stream()
                .map(s -> s.getUser().getId())
                .distinct()
                .collect(Collectors.toList());

        // End the day for each user
        for (Long userId : userIds) {
            try {
                sessionService.endDay(userId, yesterday);
                System.out.println("Auto ended day for userId=" + userId + " date=" + yesterday);
            } catch (Exception e) {
                System.err.println("Failed to auto end day for userId=" + userId + ": " + e.getMessage());
            }
        }
    }
}

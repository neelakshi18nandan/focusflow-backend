package com.focusflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.focusflow.model.Sessions;
import com.focusflow.model.StudyLog;
import com.focusflow.model.User;
import com.focusflow.repository.SessionsRepository;
import com.focusflow.repository.StudyLogRepository;
import com.focusflow.repository.UserRepository;

@Service
public class SchedulerService {

    @Autowired
    private SessionsRepository sessionsRepository;

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Runs every day at 11:59 PM.
     * For each user, reads all individual session logs from sessions table today,
     * sums them up, and writes the final daily total into study_log.
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void dailyStreakCheck() {
        System.out.println("Running daily sync...");

        LocalDate today = LocalDate.now();
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            Long userId = user.getId();

            // Get all individual logs for today from sessions table
            List<Sessions> todayLogs = sessionsRepository.findByUser_IdAndDate(userId, today);

            if (!todayLogs.isEmpty()) {
                // Sum up all individual logs into daily total
                int totalActualSec = todayLogs.stream()
                    .mapToInt(Sessions::getActualSec)
                    .sum();

                int logCount = todayLogs.size();

                // Upsert into study_log (daily total)
                Optional<StudyLog> existing = studyLogRepository.findByUser_IdAndDate(userId, today);
                StudyLog dailyTotal;

                if (existing.isPresent()) {
                    dailyTotal = existing.get();
                } else {
                    dailyTotal = new StudyLog();
                    dailyTotal.setUser(user);
                    dailyTotal.setDate(today);
                    // Use plannedSec from latest log
                    todayLogs.stream()
                        .filter(s -> s.getDurationSec() != null)
                        .mapToInt(Sessions::getDurationSec)
                        .max()
                        .ifPresent(dailyTotal::setPlannedSec);
                }

                dailyTotal.setActualSec(totalActualSec);
                dailyTotal.setSessionCount(logCount);
                studyLogRepository.save(dailyTotal);

                System.out.println("✅ " + user.getUsername()
                    + " — " + logCount + " logs, "
                    + totalActualSec + " secs total today");
            } else {
                System.out.println("❌ No logs today — " + user.getUsername() + " streak broken");
            }
        }

        System.out.println("Daily sync complete.");
    }
}
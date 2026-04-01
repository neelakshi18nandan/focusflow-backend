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

    // Runs every day at 11:59 PM
    @Scheduled(cron = "0 59 23 * * *")
    public void dailyStreakCheck() {
        System.out.println("Running daily streak check...");

        LocalDate today = LocalDate.now();
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            Long userId = user.getId();

            Optional<StudyLog> todayLog = studyLogRepository
                .findByUser_IdAndDate(userId, today);

            if (todayLog.isPresent()) {
                // User studied today — sync session count + actual secs
                List<Sessions> todaySessions = sessionsRepository
                    .findByUser_IdAndDate(userId, today);

                StudyLog log = todayLog.get();

                int totalActualSec = todaySessions.stream()
                    .mapToInt(Sessions::getActualSec)
                    .sum();

                log.setSessionCount(todaySessions.size());
                log.setActualSec(totalActualSec);
                studyLogRepository.save(log);

                System.out.println("✅ Streak active — " 
                    + user.getUsername() 
                    + " studied " + totalActualSec + " secs today");
            } else {
                // User did NOT study today — streak broken
                System.out.println("❌ No study today — " 
                    + user.getUsername() + " streak broken");
            }
        }

        System.out.println("Daily streak check complete.");
    }
}

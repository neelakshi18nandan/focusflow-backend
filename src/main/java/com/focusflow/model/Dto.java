package com.focusflow.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Dto {

    // ── Auth ──────────────────────────────────────────────────────────────────

    public static class RegisterRequest {
        @NotBlank private String username;
        @Email   private String email;
        @NotBlank private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // FIXED: Restored full 5-arg constructor + isSuccess() that UserService/AuthController use
    public static class AuthResponse {
        private Long userId;
        private String username;
        private String email;
        private String message;
        private boolean success;

        public AuthResponse(Long userId, String username, String email,
                            String message, boolean success) {
            this.userId   = userId;
            this.username = username;
            this.email    = email;
            this.message  = message;
            this.success  = success;
        }

        public Long getUserId()        { return userId; }
        public String getUsername()    { return username; }
        public String getEmail()       { return email; }
        public String getMessage()     { return message; }
        public boolean isSuccess()     { return success; }  // used by AuthController
    }

    // ── Session / StudyLog ────────────────────────────────────────────────────

    public static class SessionRequest {
        private Long userId;
        private int durationSec;
        private int plannedSec;

        public Long getUserId()            { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getDurationSec()        { return durationSec; }
        public void setDurationSec(int d)  { this.durationSec = d; }
        public int getPlannedSec()         { return plannedSec; }
        public void setPlannedSec(int p)   { this.plannedSec = p; }
    }

    public static class StudyLogResponse {
        private Long id;
        private LocalDate date;
        private int actualSec;
        private int plannedSec;
        private int sessionCount;

        public StudyLogResponse(Long id, LocalDate date, int actualSec,
                                int plannedSec, int sessionCount) {
            this.id           = id;
            this.date         = date;
            this.actualSec    = actualSec;
            this.plannedSec   = plannedSec;
            this.sessionCount = sessionCount;
        }

        public Long getId()          { return id; }
        public LocalDate getDate()   { return date; }
        public int getActualSec()    { return actualSec; }
        public int getPlannedSec()   { return plannedSec; }
        public int getSessionCount() { return sessionCount; }
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    public static class AnalyticsSummary {
        private double totalHours;
        private int totalSessions;
        private int streak;          // FIXED: was "streakDays" — frontend expects "streak"
        private double avgDailyHours;
        private List<StudyLogResponse> logs;

        public AnalyticsSummary(double totalHours, int totalSessions, int streak,
                                double avgDailyHours, List<StudyLogResponse> logs) {
            this.totalHours    = totalHours;
            this.totalSessions = totalSessions;
            this.streak        = streak;
            this.avgDailyHours = avgDailyHours;
            this.logs          = logs;
        }

        public double getTotalHours()           { return totalHours; }
        public int getTotalSessions()           { return totalSessions; }
        public int getStreak()                  { return streak; }       // FIXED: was getStreakDays()
        public void setStreak(int streak)       { this.streak = streak; }
        public double getAvgDailyHours()        { return avgDailyHours; }
        public List<StudyLogResponse> getLogs() { return logs; }
    }
}
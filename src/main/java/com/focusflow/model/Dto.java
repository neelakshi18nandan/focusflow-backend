package com.focusflow.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

public class Dto {

    // ── Auth ────────────────────────────────────────────────

    public static class RegisterRequest {
        @NotBlank private String username;
        @Email @NotBlank private String email;
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

    public static class AuthResponse {
        private Long userId;
        private String username;
        private String email;
        private String message;
        private boolean success;

        public AuthResponse() {}
        public AuthResponse(Long userId, String username, String email, String message, boolean success) {
            this.userId = userId; this.username = username; this.email = email;
            this.message = message; this.success = success;
        }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    // ── Session ─────────────────────────────────────────────

    public static class SessionRequest {
        private Long userId;
        @Min(1) private int plannedSec;
        @Min(0) private int durationSec;
        private LocalDate date;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getPlannedSec() { return plannedSec; }
        public void setPlannedSec(int plannedSec) { this.plannedSec = plannedSec; }
        public int getDurationSec() { return durationSec; }
        public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }

    public static class SessionResponse {
        private Long id;
        private int plannedSec;
        private int durationSec;
        private LocalDate date;

        public SessionResponse() {}
        public SessionResponse(Long id, int plannedSec, int durationSec, LocalDate date) {
            this.id = id; this.plannedSec = plannedSec;
            this.durationSec = durationSec; this.date = date;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public int getPlannedSec() { return plannedSec; }
        public void setPlannedSec(int plannedSec) { this.plannedSec = plannedSec; }
        public int getDurationSec() { return durationSec; }
        public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }

    // ── Study Log ───────────────────────────────────────────

    public static class StudyLogResponse {
        private Long logId;
        private LocalDate date;
        private int plannedSec;
        private int actualSec;
        private int sessionCount;
        private double plannedHours;
        private double actualHours;

        public Long getLogId() { return logId; }
        public void setLogId(Long logId) { this.logId = logId; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public int getPlannedSec() { return plannedSec; }
        public void setPlannedSec(int plannedSec) { this.plannedSec = plannedSec; }
        public int getActualSec() { return actualSec; }
        public void setActualSec(int actualSec) { this.actualSec = actualSec; }
        public int getSessionCount() { return sessionCount; }
        public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
        public double getPlannedHours() { return plannedHours; }
        public void setPlannedHours(double plannedHours) { this.plannedHours = plannedHours; }
        public double getActualHours() { return actualHours; }
        public void setActualHours(double actualHours) { this.actualHours = actualHours; }
    }

    public static class AnalyticsSummary {
        private double totalHours;
        private int totalSessions;
        private int streakDays;
        private double avgDailyHours;
        private List<StudyLogResponse> logs;

        public AnalyticsSummary() {}
        public AnalyticsSummary(double totalHours, int totalSessions, int streakDays,
                                double avgDailyHours, List<StudyLogResponse> logs) {
            this.totalHours = totalHours; this.totalSessions = totalSessions;
            this.streakDays = streakDays; this.avgDailyHours = avgDailyHours; this.logs = logs;
        }
        public double getTotalHours() { return totalHours; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        public int getStreakDays() { return streakDays; }
        public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
        public double getAvgDailyHours() { return avgDailyHours; }
        public void setAvgDailyHours(double avgDailyHours) { this.avgDailyHours = avgDailyHours; }
        public List<StudyLogResponse> getLogs() { return logs; }
        public void setLogs(List<StudyLogResponse> logs) { this.logs = logs; }
    }
}

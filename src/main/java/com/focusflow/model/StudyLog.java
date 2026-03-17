package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "study_log",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "planned_sec", nullable = false)
    private int plannedSec;

    @Column(name = "actual_sec", nullable = false)
    private int actualSec;

    @Column(name = "session_count", nullable = false)
    private int sessionCount;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getPlannedSec() { return plannedSec; }
    public void setPlannedSec(int plannedSec) { this.plannedSec = plannedSec; }
    public int getActualSec() { return actualSec; }
    public void setActualSec(int actualSec) { this.actualSec = actualSec; }
    public int getSessionCount() { return sessionCount; }
    public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
}

package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "planned_sec", nullable = false)
    private int plannedSec;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getPlannedSec() { return plannedSec; }
    public void setPlannedSec(int plannedSec) { this.plannedSec = plannedSec; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
}

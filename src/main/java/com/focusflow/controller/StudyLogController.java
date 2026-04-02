package com.focusflow.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.focusflow.model.Dto.*;
import com.focusflow.model.Sessions;
import com.focusflow.model.User;
import com.focusflow.repository.SessionsRepository;
import com.focusflow.repository.UserRepository;
import com.focusflow.service.StudyLogService;

@RestController
@RequestMapping("/api/logs")
public class StudyLogController {

    @Autowired
    private StudyLogService studyLogService;

    // FIXED Bug 3: injected so we can write to sessions table
    @Autowired
    private SessionsRepository sessionsRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * POST /api/logs/session
     * Called after every pomodoro completes.
     * 1) Inserts a row into sessions table (Bug 3 fix)
     * 2) Upserts today's study_log row
     * Body: { "userId": 1, "durationSec": 1500, "plannedSec": 7200 }
     */
    @PostMapping("/session")
    public ResponseEntity<StudyLogResponse> recordSession(@RequestBody SessionRequest req) {
        // FIXED Bug 3: save individual session row first
        User user = userRepository.findById(req.getUserId()).orElse(null);
        if (user != null) {
            Sessions session = new Sessions();
            session.setUser(user);
            session.setDate(LocalDate.now());
            session.setActualSec(req.getDurationSec());
            session.setPlannedSec(req.getPlannedSec());
            session.setLoggedAt(LocalDateTime.now());
            sessionsRepository.save(session);
        }

        // Then upsert the daily study_log (session_count is recalculated from sessions table)
        return ResponseEntity.ok(
            studyLogService.recordSession(req.getUserId(), req.getDurationSec(), req.getPlannedSec())
        );
    }

    /**
     * GET /api/logs/{userId}
     * Full analytics for the analysis page.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AnalyticsSummary> getAnalytics(@PathVariable Long userId) {
        return ResponseEntity.ok(studyLogService.getAnalytics(userId));
    }

    /**
     * GET /api/logs/{userId}/range?from=2026-01-01&to=2026-01-31
     * Logs in a date range for monthly charts.
     */
    @GetMapping("/{userId}/range")
    public ResponseEntity<List<StudyLogResponse>> getLogsInRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(studyLogService.getLogsInRange(userId, from, to));
    }
}
package com.focusflow.controller;

import com.focusflow.model.Dto.*;
import com.focusflow.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Handles study sessions (timer events).
 *
 * POST /api/sessions                        → save a completed pomodoro session
 * POST /api/sessions/end-day               → end the day: save to study_log, delete sessions
 * GET  /api/sessions/{userId}              → get all sessions for a user
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * Called when a single pomodoro cycle completes.
     * Saves session and upserts daily study_log.
     */
    @PostMapping
    public ResponseEntity<SessionResponse> saveSession(@Valid @RequestBody SessionRequest req) {
        SessionResponse res = sessionService.saveSession(req);
        return ResponseEntity.ok(res);
    }

    /**
     * Called when user clicks "End the Day" OR 24hr auto-end triggers.
     * Saves final totals to study_log and deletes session rows.
     *
     * Body: { "userId": 1, "date": "2026-03-18" }
     */
    @PostMapping("/end-day")
    public ResponseEntity<Map<String, Object>> endDay(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        LocalDate date = body.containsKey("date")
                ? LocalDate.parse(body.get("date").toString())
                : LocalDate.now();

        sessionService.endDay(userId, date);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Day ended successfully",
                "date", date.toString()
        ));
    }

    /**
     * Get all sessions for a user (for history view).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<SessionResponse>> getSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUser(userId));
    }
}
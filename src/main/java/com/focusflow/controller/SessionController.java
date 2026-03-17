package com.focusflow.controller;

import com.focusflow.model.Dto.*;
import com.focusflow.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles study sessions (timer events).
 *
 * POST /api/sessions           →  save a completed session
 * GET  /api/sessions/{userId}  →  get all sessions for a user
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * Called when a focus session ends (timer complete OR user stops).
     * Also auto-updates the daily study_log.
     *
     * Body: {
     *   "userId": 1,
     *   "plannedSec": 1500,    // e.g. 25 minutes = 1500 sec
     *   "durationSec": 1320,   // how long they actually ran
     *   "date": "2026-03-15"
     * }
     */
    @PostMapping
    public ResponseEntity<SessionResponse> saveSession(@Valid @RequestBody SessionRequest req) {
        SessionResponse res = sessionService.saveSession(req);
        return ResponseEntity.ok(res);
    }

    /**
     * Get all sessions for a user (optional, for a history view).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<SessionResponse>> getSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(sessionService.getSessionsByUser(userId));
    }
}

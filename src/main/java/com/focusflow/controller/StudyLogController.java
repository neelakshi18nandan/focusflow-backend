package com.focusflow.controller;

import java.time.LocalDate;
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
import com.focusflow.model.Dto.AnalyticsSummary;
import com.focusflow.model.Dto.SessionRequest;
import com.focusflow.model.Dto.StudyLogResponse;
import com.focusflow.service.StudyLogService;

@RestController
@RequestMapping("/api/logs")
public class StudyLogController {

    @Autowired
    private StudyLogService studyLogService;

    /**
     * POST /api/logs/session
     * Called after every pomodoro completes OR timer is reset mid-session.
     * - Saves individual log into sessions table
     * - Updates daily total in study_log table
     * Body: { "userId": 1, "durationSec": 1500, "plannedSec": 7200 }
     */
    @PostMapping("/session")
    public ResponseEntity<StudyLogResponse> recordSession(@RequestBody SessionRequest req) {
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
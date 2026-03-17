package com.focusflow.controller;

import com.focusflow.model.Dto.*;
import com.focusflow.service.StudyLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Serves data for the analytics/analysis page.
 *
 * GET /api/logs/{userId}                          →  full summary + all logs
 * GET /api/logs/{userId}/range?from=...&to=...    →  logs in a date range
 */
@RestController
@RequestMapping("/api/logs")
public class StudyLogController {

    @Autowired
    private StudyLogService studyLogService;

    /**
     * Full analytics for a user — used to populate the analysis.html page.
     * Returns total hours, streak, sessions, and daily log list.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AnalyticsSummary> getAnalytics(@PathVariable Long userId) {
        return ResponseEntity.ok(studyLogService.getAnalytics(userId));
    }

    /**
     * Logs in a specific date range — used for monthly/weekly charts.
     * Example: GET /api/logs/1/range?from=2026-01-01&to=2026-01-31
     */
    @GetMapping("/{userId}/range")
    public ResponseEntity<List<StudyLogResponse>> getLogsInRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(studyLogService.getLogsInRange(userId, from, to));
    }
}

/**
 * focusflow-api.js
 * ─────────────────────────────────────────────────────────
 * Drop this file in the same folder as your HTML files.
 * Add <script src="focusflow-api.js"></script> to each page.
 *
 * It handles all communication with the Spring Boot backend.
 * The logged-in user is stored in sessionStorage so it persists
 * across pages but clears when the browser tab is closed.
 * ─────────────────────────────────────────────────────────
 */

const API_BASE = 'http://localhost:8080/api';

// ── Auth helpers ──────────────────────────────────────────

/**
 * Register a new user.
 * @param {string} username
 * @param {string} email
 * @param {string} password  (plain text — hashed on the server)
 * @returns {Promise<{success, userId, username, message}>}
 */
async function ffRegister(username, email, password) {
    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        const data = await res.json();
        if (data.success) ffSaveUser(data);
        return data;
    } catch (e) {
        console.error('Register error:', e);
        return { success: false, message: 'Could not reach server' };
    }
}

/**
 * Login with username + password.
 * @returns {Promise<{success, userId, username, message}>}
 */
async function ffLogin(username, password) {
    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (data.success) ffSaveUser(data);
        return data;
    } catch (e) {
        console.error('Login error:', e);
        return { success: false, message: 'Could not reach server' };
    }
}

/** Save logged-in user to sessionStorage */
function ffSaveUser(data) {
    sessionStorage.setItem('ff_userId',   data.userId);
    sessionStorage.setItem('ff_username', data.username);
}

/** Get current logged-in user (or null) */
function ffGetUser() {
    const userId   = sessionStorage.getItem('ff_userId');
    const username = sessionStorage.getItem('ff_username');
    if (!userId) return null;
    return { userId: Number(userId), username };
}

/** Logout — clears sessionStorage */
function ffLogout() {
    sessionStorage.removeItem('ff_userId');
    sessionStorage.removeItem('ff_username');
}

// ── Session helpers ───────────────────────────────────────

/**
 * Save a completed study session.
 * Call this when the timer ends OR when "End the Day" is clicked.
 *
 * @param {number} plannedSec   - how many seconds the user set as goal
 * @param {number} durationSec  - how many seconds they actually studied
 * @returns {Promise<{id, plannedSec, durationSec, date} | null>}
 *
 * Example usage in index.html:
 *   await ffSaveSession(1500, timerElapsedSeconds);
 */
async function ffSaveSession(plannedSec, durationSec) {
    const user = ffGetUser();
    if (!user) {
        console.warn('ffSaveSession: no user logged in');
        return null;
    }

    const today = new Date().toISOString().split('T')[0]; // "2026-03-15"

    try {
        const res = await fetch(`${API_BASE}/sessions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId:      user.userId,
                plannedSec:  plannedSec,
                durationSec: durationSec,
                date:        today
            })
        });
        return await res.json();
    } catch (e) {
        console.error('Save session error:', e);
        return null;
    }
}

// ── Analytics helpers ─────────────────────────────────────

/**
 * Get full analytics summary for the current user.
 * Used by analysis.html to replace the dummy weekData() function.
 *
 * @returns {Promise<{totalHours, totalSessions, streakDays, avgDailyHours, logs[]}>}
 */
async function ffGetAnalytics() {
    const user = ffGetUser();
    if (!user) return null;
    try {
        const res = await fetch(`${API_BASE}/logs/${user.userId}`);
        return await res.json();
    } catch (e) {
        console.error('Analytics error:', e);
        return null;
    }
}

/**
 * Get logs for a specific date range.
 * @param {string} from - "YYYY-MM-DD"
 * @param {string} to   - "YYYY-MM-DD"
 * @returns {Promise<Array<{date, plannedHours, actualHours, sessionCount}>>}
 */
async function ffGetLogsInRange(from, to) {
    const user = ffGetUser();
    if (!user) return [];
    try {
        const res = await fetch(`${API_BASE}/logs/${user.userId}/range?from=${from}&to=${to}`);
        return await res.json();
    } catch (e) {
        console.error('Range logs error:', e);
        return [];
    }
}

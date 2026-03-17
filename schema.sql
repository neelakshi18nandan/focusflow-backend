-- ═══════════════════════════════════════════════════
--  Focus Flow — MySQL Database Schema
--  Run this in MySQL Workbench or mysql CLI before
--  starting the Spring Boot server.
-- ═══════════════════════════════════════════════════

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS focusflow_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE focusflow_db;

-- ─────────────────────────────────────────────────
--  TABLE 1: user
--  Stores registered users.
--  password_hash is BCrypt — never plain text.
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- ─────────────────────────────────────────────────
--  TABLE 2: session
--  One row per focus session (one timer run).
--  planned_sec  = what the user set as goal
--  duration_sec = how long they actually studied
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS session (
    id           BIGINT  NOT NULL AUTO_INCREMENT,
    user_id      BIGINT  NOT NULL,
    planned_sec  INT     NOT NULL,
    date         DATE    NOT NULL,
    duration_sec INT     NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────
--  TABLE 3: study_log
--  Daily summary — ONE row per (user, date).
--  Auto-updated every time a session is saved.
--  planned_sec   = sum of all planned_sec that day
--  actual_sec    = sum of all duration_sec that day
--  session_count = how many sessions that day
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS study_log (
    log_id        BIGINT NOT NULL AUTO_INCREMENT,
    user_id       BIGINT NOT NULL,
    date          DATE   NOT NULL,
    planned_sec   INT    NOT NULL DEFAULT 0,
    actual_sec    INT    NOT NULL DEFAULT 0,
    session_count INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (log_id),
    UNIQUE KEY uq_user_date (user_id, date),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────
--  Sample data (optional — delete if not needed)
-- ─────────────────────────────────────────────────
-- INSERT INTO user (username, email, password_hash) VALUES
--   ('testuser', 'test@email.com', '$2a$10$examplehashhere');
-- (Use the /api/auth/register endpoint to create real users)

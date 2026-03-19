package com.focusflow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.focusflow.model.StudyLog;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    // For analytics page — all logs newest first
    List<StudyLog> findByUserIdOrderByDateDesc(Long userId);

    // For monthly charts
    List<StudyLog> findByUserIdAndDateBetweenOrderByDate(Long userId, LocalDate from, LocalDate to);

    // For upsert — find today's row
    Optional<StudyLog> findByUserIdAndDate(Long userId, LocalDate date);
}
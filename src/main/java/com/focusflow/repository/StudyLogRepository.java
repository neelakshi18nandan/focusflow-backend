package com.focusflow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.focusflow.model.StudyLog;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
    List<StudyLog> findByUser_IdOrderByDateDesc(Long userId);
    List<StudyLog> findByUser_IdAndDateBetweenOrderByDate(Long userId, LocalDate from, LocalDate to);
    Optional<StudyLog> findByUser_IdAndDate(Long userId, LocalDate date);
}
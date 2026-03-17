package com.focusflow.repository;

import com.focusflow.model.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    // Find log for a specific user on a specific date (unique per day)
    Optional<StudyLog> findByUserIdAndDate(Long userId, LocalDate date);

    // All logs for a user (for analytics page)
    List<StudyLog> findByUserIdOrderByDateDesc(Long userId);

    // Logs in a date range (for monthly/weekly charts)
    List<StudyLog> findByUserIdAndDateBetweenOrderByDate(Long userId, LocalDate start, LocalDate end);
}

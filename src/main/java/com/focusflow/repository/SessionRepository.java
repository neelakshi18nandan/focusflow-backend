package com.focusflow.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.focusflow.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // All sessions for a user
    List<Session> findByUserId(Long userId);

    // Sessions on a specific date for a user
    List<Session> findByUserIdAndDate(Long userId, LocalDate date);

    // ALL sessions on a specific date (for scheduler - across all users)
    List<Session> findByDate(LocalDate date);

    // Sessions in a date range
    List<Session> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    // Total seconds studied for a user on a date
    @Query("SELECT COALESCE(SUM(s.durationSec), 0) FROM Session s WHERE s.user.id = :userId AND s.date = :date")
    int sumDurationByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Count sessions on a date
    int countByUserIdAndDate(Long userId, LocalDate date);
}
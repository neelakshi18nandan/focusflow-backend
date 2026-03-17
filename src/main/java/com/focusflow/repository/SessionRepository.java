package com.focusflow.repository;

import com.focusflow.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // All sessions for a user
    List<Session> findByUserId(Long userId);

    // Sessions on a specific date
    List<Session> findByUserIdAndDate(Long userId, LocalDate date);

    // Sessions in a date range (used by analytics)
    List<Session> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    // Total seconds studied for a user on a date
    @Query("SELECT COALESCE(SUM(s.durationSec), 0) FROM Session s WHERE s.user.id = :userId AND s.date = :date")
    int sumDurationByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Count sessions on a date
    int countByUserIdAndDate(Long userId, LocalDate date);
}

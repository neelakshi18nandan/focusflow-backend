package com.focusflow.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.focusflow.model.Sessions;

@Repository
public interface SessionsRepository extends JpaRepository<Sessions, Long> {
    List<Sessions> findByUser_IdOrderByLoggedAtDesc(Long userId);
    List<Sessions> findByUser_IdAndDate(Long userId, LocalDate date);
}
package com.frytes.cinemaPlus.repository;

import com.frytes.cinemaPlus.content.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsByHallIdAndStartTimeBeforeAndEndTimeAfter(Long id, LocalDateTime end, LocalDateTime start);
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}

package com.frytes.cinemaPlus.content.repository;

import com.frytes.cinemaPlus.content.entity.Session;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsByHallIdAndStartTimeBeforeAndEndTimeAfter(Long id, LocalDateTime end, LocalDateTime start);

    @EntityGraph(attributePaths = {"movie", "hall"})
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Session> findByMovieIdAndStartTimeAfter(Long movieId, LocalDateTime now);
    boolean existsByHallIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
            Long hallId, LocalDateTime endTime, LocalDateTime startTime, Long id
    );
}

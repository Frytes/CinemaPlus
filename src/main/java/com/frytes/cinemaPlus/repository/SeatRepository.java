package com.frytes.cinemaPlus.repository;

import com.frytes.cinemaPlus.content.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}

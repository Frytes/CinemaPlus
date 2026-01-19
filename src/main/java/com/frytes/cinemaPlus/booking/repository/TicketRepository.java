package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findAllBySessionId(Long sessionId);
    List<Ticket> findAllBySessionIdAndSeatIdIn(Long sessionId, List<Long> seatIds);
}

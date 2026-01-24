package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.entity.OutboxEvent;
import com.frytes.cinemaPlus.booking.entity.enumps.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent,Long> {
    List<OutboxEvent> findAllByStatus(OutboxStatus status);
}

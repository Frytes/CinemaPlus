package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"tickets", "tickets.session"})
    Optional<Order> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long id, OrderStatus orderStatus);
    @EntityGraph(attributePaths = {
            "tickets",
            "tickets.seat",
            "tickets.session",
            "tickets.session.movie",
            "tickets.session.hall"
    })
    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"tickets", "tickets.seat"})
    Optional<Order> findWithDetailsById(Long id);

}

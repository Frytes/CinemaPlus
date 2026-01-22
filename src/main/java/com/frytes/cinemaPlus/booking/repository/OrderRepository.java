package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long id, OrderStatus orderStatus);
    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}

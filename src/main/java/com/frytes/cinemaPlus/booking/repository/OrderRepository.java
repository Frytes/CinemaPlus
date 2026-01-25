package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    Optional<Order> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long id, OrderStatus orderStatus);

    @EntityGraph(attributePaths = {
            "tickets",
            "tickets.seat",
            "tickets.session",
            "tickets.session.movie",
            "tickets.session.hall"
    })
    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);


    @EntityGraph(attributePaths = {
            "tickets", "tickets.seat", "tickets.session",
            "tickets.session.movie", "tickets.session.hall"
    })
    Optional<Order> findWithDetailsById(Long id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "user",
            "tickets", "tickets.seat", "tickets.session",
            "tickets.session.movie", "tickets.session.hall"
    })
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findLockedWithDetailsById(Long id);


    @EntityGraph(attributePaths = {
            "user",
            "tickets",
            "tickets.seat",
            "tickets.session"
    })
    List<Order> findAllByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);

    @Modifying
    @Query(value = "UPDATE orders SET created_at = :date WHERE id = :id", nativeQuery = true)
    void forceUpdateCreatedAt(Long id, LocalDateTime date);
}

package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.dto.MovieStatDto;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.math.BigDecimal;
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

    //Сумма продаж за период
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'PAID' AND o.createdAt >= :start AND o.createdAt <= :end")
    BigDecimal countRevenueBetween(LocalDateTime start, LocalDateTime end);

    //Кол-во билетов за период
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.order.status = 'PAID' AND t.order.createdAt >= :start AND t.order.createdAt <= :end")
    Long countTicketsBetween(LocalDateTime start, LocalDateTime end);

    //Топ фильмов по выручке
    @Query("SELECT new com.frytes.cinemaPlus.booking.dto.MovieStatDto(" +
            "m.title, " +
            "SUM(t.price), " +
            "COUNT(t)) " +
            "FROM Ticket t " +
            "JOIN t.session s " +
            "JOIN s.movie m " +
            "JOIN t.order o " +
            "WHERE o.status = :status " +
            "GROUP BY m.title " +
            "ORDER BY SUM(t.price) DESC")
    List<MovieStatDto> findTopMoviesByRevenue(OrderStatus status, Pageable pageable);
}

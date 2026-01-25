package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.PricingRule;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.booking.service.pricing.PriceCalculator;
import com.frytes.cinemaPlus.booking.service.pricing.PricingRulesService;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.common.exception.SeatAlreadySoldException;
import com.frytes.cinemaPlus.common.exception.UserAlreadyExistsException;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.content.dto.enums.SocketStatus;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.notification.service.SocketNotificationService;
import com.frytes.cinemaPlus.repository.SeatRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final SessionRepository sessionRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final BookingLockService bookingLockService;
    private final PriceCalculator priceCalculator;
    private final PricingRulesService pricingRulesService;
    private final SocketNotificationService socketService;

    @Transactional
    public Order createBooking(BookingRequest request, User user) {
        Session session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс не найден"));

        List<Ticket> soldTickets = ticketRepository.findAllBySessionIdAndSeatIdIn(
                session.getId(),
                request.seatIds()
        );

        if (!soldTickets.isEmpty()) {
            throw new SeatAlreadySoldException("Выбранные места уже куплены");
        }

        List<Seat> seats = seatRepository.findAllById(request.seatIds());

        if (seats.size() != request.seatIds().size()) {
            throw new ResourceNotFoundException("Некоторые места не найдены");
        }

        for (Seat seat : seats) {
            if (!seat.getHall().getId().equals(session.getHall().getId())) {
                throw new IllegalArgumentException("Место " + seat.getSeatNumber() + " не принадлежит залу этого сеанса!");
            }
        }
        List<Long> lockedSeats = new ArrayList<>();
        try {
            for (Long seatId : request.seatIds()) {
                boolean success = bookingLockService.acquireLock(session.getId(), seatId, user.getId());
                if (!success) {
                    throw new UserAlreadyExistsException("Место " + seatId + " уже выбрано другим пользователем");
                }
                lockedSeats.add(seatId);
            }
        } catch (RuntimeException e) {
            for (Long seatId : lockedSeats) {
                bookingLockService.releaseLock(session.getId(), seatId, user.getId());
            }
            throw e;
        }

        BigDecimal totalPrice = priceCalculator.calculateTotal(session, seats);

        try {
            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .totalPrice(totalPrice)
                    .tickets(new ArrayList<>())
                    .build();
            orderRepository.save(order);

            for (Seat seat : seats) {
                Ticket ticket = Ticket.builder()
                        .session(session)
                        .seat(seat)
                        .order(order)
                        .build();


                order.getTickets().add(ticket);
                socketService.sendSeatUpdate(session.getId(), seat, SocketStatus.LOCKED);
            }

            return orderRepository.save(order);
        } catch (RuntimeException e) {

            for (Long seatId : lockedSeats) {
                bookingLockService.releaseLock(session.getId(), seatId, user.getId());
            }
            throw e;
        }

    }
    @Transactional(readOnly = true)
    public List<SeatStatusDto> getSeatsForSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс не найден"));

        List<Seat> allSeats = session.getHall().getSeats();

        Map<String, PricingRule> rules = pricingRulesService.getAllRulesMap();
        PricingRule vipRule = rules.get("VIP_SURCHARGE");
        BigDecimal vipSurcharge = (vipRule != null && Boolean.TRUE.equals(vipRule.getIsActive()))
                ? vipRule.getAmount()
                : BigDecimal.ZERO;

        Set<Long> soldSeatIds = ticketRepository.findAllBySessionId(sessionId).stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        Set<Long> lockedSeatIds = bookingLockService.getLockedSeats(sessionId);

        return allSeats.stream()
                .map(seat -> {
                    boolean isSold = soldSeatIds.contains(seat.getId());
                    boolean isLocked = lockedSeatIds.contains(seat.getId());
                    boolean isBooked = isSold || isLocked;

                    BigDecimal seatPrice = session.getBasePrice();
                    if (seat.getType() == SeatType.VIP) {
                        seatPrice = seatPrice.add(vipSurcharge);
                    }

                    return new SeatStatusDto(
                            seat.getId(),
                            seat.getRowIndex(),
                            seat.getColIndex(),
                            seat.getSeatNumber(),
                            seat.getType().name(),
                            isBooked,
                            seatPrice
                    );
                })
                .toList();
    }
    @Transactional
    public void cancelBooking(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Нельзя отменить чужой заказ");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Можно отменить только неоплаченный заказ");
        }

        for (Ticket ticket : order.getTickets()) {
            bookingLockService.releaseLock(
                    ticket.getSession().getId(),
                    ticket.getSeat().getId(),
                    user.getId()
            );

            socketService.sendSeatUpdate(
                    ticket.getSession().getId(),
                    ticket.getSeat(),
                    SocketStatus.AVAILABLE
            );
        }
        ticketRepository.deleteAll(order.getTickets());
        order.getTickets().clear();
        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
    @Transactional(readOnly = true)
    public java.util.Optional<Order> findPendingBooking(Long sessionId, User user) {
        if (user == null) {
            return Optional.empty();
        }
        return orderRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), OrderStatus.PENDING)
                .filter(order -> {
                    if (!order.getTickets().isEmpty())
                        return order.getTickets().getFirst().getSession().getId().equals(sessionId);
                    return false;
                });
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(User user) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public Map<String, Object> getOrderQrData(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));

        if (order.getTickets() == null || order.getTickets().isEmpty()) {
            return Map.of(
                    "orderId", orderId,
                    "error", "Нет данных о билетах",
                    "status", order.getStatus().name()
            );
        }

        Ticket firstTicket = order.getTickets().getFirst();
        Session session = firstTicket.getSession();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<Map<String, String>> seatsInfo = order.getTickets().stream()
                .map(ticket -> {
                    Seat seat = ticket.getSeat();
                    return Map.of(
                            "row", String.valueOf(seat.getRowIndex() + 1),
                            "seat", seat.getSeatNumber(),
                            "type", seat.getType().name()
                    );
                })
                .toList();
        return Map.of(
                "order_id", orderId,
                "movie", session.getMovie().getTitle(),
                "date", session.getStartTime().format(dateFormatter),
                "time", session.getStartTime().format(timeFormatter),
                "tickets", seatsInfo

        );

    }
}

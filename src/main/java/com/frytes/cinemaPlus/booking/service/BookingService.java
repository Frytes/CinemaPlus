package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.common.exception.SeatAlreadySoldException;
import com.frytes.cinemaPlus.common.exception.UserAlreadyExistsException;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.repository.SeatRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;




@Service
@RequiredArgsConstructor
public class BookingService {

    private final SessionRepository sessionRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final BookingLockService bookingLockService;


    @Transactional
    public void createBooking(BookingRequest request, User user) {
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

        try {
            Order order = new Order();
            order.setUser(user);
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            for (Seat seat : seats) {
                Ticket ticket = new Ticket();
                ticket.setSession(session);
                ticket.setSeat(seat);
                ticket.setOrder(order);
                ticketRepository.save(ticket);
            }


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

        Set<Long> soldSeatIds = ticketRepository.findAllBySessionId(sessionId).stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        Set<Long> lockedSeatIds = bookingLockService.getLockedSeats(sessionId);

        return allSeats.stream()
                .map(seat -> {
                    boolean isSold = soldSeatIds.contains(seat.getId());
                    boolean isLocked = lockedSeatIds.contains(seat.getId());

                    boolean isBooked = isSold || isLocked;

                    return new SeatStatusDto(
                            seat.getId(),
                            seat.getRowIndex(),
                            seat.getColIndex(),
                            seat.getSeatNumber(),
                            seat.getType().name(),
                            isBooked,
                            session.getBasePrice()
                    );
                })
                .toList();
    }

}

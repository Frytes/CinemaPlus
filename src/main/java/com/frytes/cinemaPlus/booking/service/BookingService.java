package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.repository.SeatRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public void createBooking(BookingRequest request, User user) {
        Session session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс не найден"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        for (Long seatId : request.seatIds()) {

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Место не найдено"));

            Ticket ticket = new Ticket();
            ticket.setSession(session);
            ticket.setSeat(seat);
            ticket.setOrder(order);
            ticketRepository.save(ticket);
        }


    }
    @Transactional(readOnly = true)
    public List<SeatStatusDto> getSeatsForSession (Long sessionId){
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс не найден"));
        List<Seat> allSeats = session.getHall().getSeats();

        Set<Long> bookedSeatIds = ticketRepository.findAllBySessionId(sessionId).stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        return allSeats.stream()
                .map(seat -> new SeatStatusDto(
                        seat.getId(),
                        seat.getRowIndex(),
                        seat.getColIndex(),
                        seat.getSeatNumber(),
                        seat.getType().name(),
                        bookedSeatIds.contains(seat.getId()),
                        session.getBasePrice()
                ))
                .toList();

    }
}

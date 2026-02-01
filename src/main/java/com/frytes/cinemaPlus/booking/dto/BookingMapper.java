package com.frytes.cinemaPlus.booking.dto;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BookingMapper {

    @Value("${cinema.rules.lock-duration-minutes}")
    protected int lockDurationMinutes;

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "seatIds", expression = "java(mapTicketsToIds(order.getTickets()))")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "expiresInSeconds", expression = "java(calculateExpiresIn(order))")
    public abstract BookingResponse toResponse(Order order);


    protected List<Long> mapTicketsToIds(List<Ticket> tickets) {
        if (tickets == null) return java.util.Collections.emptyList();
        return tickets.stream()
                .map(ticket -> ticket.getSeat().getId())
                .toList();
    }

    protected Long calculateExpiresIn(Order order) {
        if (order.getCreatedAt() == null) return 0L;

        LocalDateTime expiryTime = order.getCreatedAt().plusMinutes(lockDurationMinutes);
        long secondsLeft = Duration.between(LocalDateTime.now(), expiryTime).getSeconds();

        return secondsLeft > 0 ? secondsLeft : 0L;
    }

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "tickets", source = "tickets")
    public abstract OrderHistoryDto toHistoryDto(Order order);

    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "movieTitle", source = "session.movie.title")
    @Mapping(target = "hallName", source = "session.hall.name")
    @Mapping(target = "startTime", source = "session.startTime")
    @Mapping(target = "seatNumber", source = "seat.seatNumber")
    @Mapping(target = "rowIndex", source = "seat.rowIndex")
    @Mapping(target = "sessionId", source = "session.id")
    public abstract OrderHistoryDto.TicketDto toTicketDto(Ticket ticket);
}
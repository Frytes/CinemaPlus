package com.frytes.cinemaPlus.booking.dto;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BookingMapper {

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "seatIds", expression = "java(mapTicketsToIds(order.getTickets()))")
    @Mapping(target = "createdAt", source = "createdAt")
    public abstract BookingResponse toResponse(Order order);


    protected List<Long> mapTicketsToIds(List<Ticket> tickets) {
        if (tickets == null) return java.util.Collections.emptyList();
        return tickets.stream()
                .map(ticket -> ticket.getSeat().getId())
                .toList();
    }
}
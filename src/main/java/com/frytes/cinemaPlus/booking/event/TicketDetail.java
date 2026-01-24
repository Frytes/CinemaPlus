package com.frytes.cinemaPlus.booking.event;

import com.frytes.cinemaPlus.content.entity.enumps.SeatType;

public record TicketDetail(
        String seatNumber,
        int rowIndex,
        int colIndex,
        SeatType type
) {}
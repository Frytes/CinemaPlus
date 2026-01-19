package com.frytes.cinemaPlus.content.dto;

import java.math.BigDecimal;

public record SeatStatusDto(
        Long id,
        Integer rowIndex,
        Integer colIndex,
        String seatNumber,
        String type,
        boolean isBooked,
        BigDecimal price
) {}

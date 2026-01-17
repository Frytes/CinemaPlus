package com.frytes.cinemaPlus.content.dto;

public record SeatDto(
        Integer rowIndex,
        Integer colIndex,
        String seatNumber,
        String type
) {}

package com.frytes.cinemaPlus.content.dto;

import com.frytes.cinemaPlus.content.dto.enums.SocketStatus;


public record SocketSeatMessage(
        Long sessionId,
        Long seatId,
        int rowIndex,
        int colIndex,
        String seatNumber,
        SocketStatus status
) {}
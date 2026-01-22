package com.frytes.cinemaPlus.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderHistoryDto(
        Long orderId,
        LocalDateTime createdAt,
        BigDecimal totalPrice,
        String status,
        List<TicketDto> tickets
) {
    public record TicketDto(
            Long ticketId,
            String movieTitle,
            String hallName,
            LocalDateTime startTime,
            String seatNumber,
            Integer rowIndex,
            Long sessionId
    ) {}
}

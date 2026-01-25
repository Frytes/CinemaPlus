package com.frytes.cinemaPlus.booking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingPaidEvent(
        Long orderId,
        Long userId,
        String userEmail,
        String movieTitle,
        Long sessionId,
        String hall,
        List<TicketDetail> tickets,
        BigDecimal amount,
        LocalDateTime eventTime
) {}
package com.frytes.cinemaPlus.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long orderId,
        BigDecimal totalPrice,
        String status,
        List<Long> seatIds,
        LocalDateTime createdAt
) {}
package com.frytes.cinemaPlus.booking.dto;

import java.math.BigDecimal;

public record BookingResponse(
        Long orderId,
        BigDecimal totalPrice,
        String status
) {}
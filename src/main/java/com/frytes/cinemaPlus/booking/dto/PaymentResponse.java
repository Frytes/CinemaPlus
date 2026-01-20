package com.frytes.cinemaPlus.booking.dto;

public record PaymentResponse(
        Long orderId,
        String status,
        String message
) {}
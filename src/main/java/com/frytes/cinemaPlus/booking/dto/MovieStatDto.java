package com.frytes.cinemaPlus.booking.dto;

import java.math.BigDecimal;

public record MovieStatDto(
        String title,
        BigDecimal revenue,
        Long ticketsSold
) {}
package com.frytes.cinemaPlus.booking.dto;

import java.math.BigDecimal;

public record UpdateRuleRequest(
        BigDecimal amount,
        Boolean isActive
) {}

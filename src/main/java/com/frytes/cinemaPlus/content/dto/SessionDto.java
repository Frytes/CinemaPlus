package com.frytes.cinemaPlus.content.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionDto(
        Long id,
        Long movieId,
        String movieTitle,
        Long hallId,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price
) {}

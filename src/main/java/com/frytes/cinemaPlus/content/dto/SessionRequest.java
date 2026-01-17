package com.frytes.cinemaPlus.content.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionRequest(
        @NotNull(message = "ID фильма обязателен")
        Long movieId,

        @NotNull(message = "ID зала обязателен")
        Long hallId,

        @NotNull(message = "Время начала обязательно")
        @Future(message = "Сеанс должен быть в будущем")
        LocalDateTime startTime,

        @NotNull(message = "Цена билета обязательна")
        @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
        BigDecimal basePrice,

        @Min(value = 0, message = "Реклама не может быть отрицательной")
        Integer adsMinutes,

        @Min(value = 0, message = "Уборка не может быть отрицательной")
        Integer cleanupMinutes
) {}
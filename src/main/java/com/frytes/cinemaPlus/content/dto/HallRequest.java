package com.frytes.cinemaPlus.content.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HallRequest(
        @NotBlank String name,

        @NotNull @Min(1) Integer width,  // Ширина сетки
        @NotNull @Min(1) Integer height, // Высота сетки

        String defaultType
) {}
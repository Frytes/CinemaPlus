package com.frytes.cinemaPlus.content.dto;

import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HallRequest(
        @NotBlank String name,

        @NotNull @Min(1) Integer width,
        @NotNull @Min(1) Integer height,

        List<SeatConfigDto> seats
) {
    public record SeatConfigDto(
            int row,
            int col,
            SeatType type,
            String seatNumber
    ) {}
}
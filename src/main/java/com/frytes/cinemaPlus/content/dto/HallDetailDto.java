package com.frytes.cinemaPlus.content.dto;

import java.util.List;

public record HallDetailDto(
        Long id,
        String name,
        Integer width,
        Integer height,
        List<SeatDto> seats
) {}

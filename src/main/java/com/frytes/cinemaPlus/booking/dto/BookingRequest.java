package com.frytes.cinemaPlus.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookingRequest(
        @NotNull Long sessionId,
        @NotEmpty List<Long> seatIds
) {}
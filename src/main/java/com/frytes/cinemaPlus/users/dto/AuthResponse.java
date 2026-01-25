package com.frytes.cinemaPlus.users.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}

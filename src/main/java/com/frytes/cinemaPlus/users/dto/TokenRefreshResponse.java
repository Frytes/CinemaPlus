package com.frytes.cinemaPlus.users.dto;

public record TokenRefreshResponse (
        String accessToken,
        String refreshToken
){}

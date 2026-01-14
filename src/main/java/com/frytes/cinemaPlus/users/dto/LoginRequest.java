package com.frytes.cinemaPlus.users.dto;

public record LoginRequest(
        String email,
        String password
) {}
package com.frytes.cinemaPlus.users.event;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String username
) {}
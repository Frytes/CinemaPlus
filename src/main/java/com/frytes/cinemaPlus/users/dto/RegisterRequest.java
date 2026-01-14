package com.frytes.cinemaPlus.users.dto;

import com.frytes.cinemaPlus.users.entity.Role;

public record RegisterRequest(
        String username,
        String email,
        String password,
        Role role
) {}
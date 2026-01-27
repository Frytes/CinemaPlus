package com.frytes.cinemaPlus.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^[^<>&]+$", message = "Недопустимые символы")
        String username,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Некорректный формат email")
        String email
) {}
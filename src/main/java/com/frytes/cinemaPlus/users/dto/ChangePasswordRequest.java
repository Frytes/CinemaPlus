package com.frytes.cinemaPlus.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=]+$", message = "Пароль содержит недопустимые символы")
        String newPassword
) {}
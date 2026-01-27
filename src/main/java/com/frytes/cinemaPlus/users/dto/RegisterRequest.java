package com.frytes.cinemaPlus.users.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
        @Pattern(
                regexp = "^[^<>&]+$",
                message = "Имя содержит недопустимые символы"
        )
        String username,

        @NotBlank(message = "Email не может быть пустым")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Некорректный формат email"
        )
        String email,

        @Size(min = 8, message = "Пароль должен состоять минимум из 8 символов")
        @Pattern(
                regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=]+$",
                message = "Пароль содержит недопустимые символы"
        )
        String password
) {}
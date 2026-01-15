package com.frytes.cinemaPlus.users.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя не может быть пустым")
        String username,

        @Email(message = "Не правильный mail")
        @NotBlank(message = "Email не может быть пустым")
        String email,

        @Size(min = 6, message = "Пароль должен состоять минимум из 6 символов")
        String password
) {}
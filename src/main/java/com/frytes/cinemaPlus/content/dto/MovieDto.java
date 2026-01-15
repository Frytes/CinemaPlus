package com.frytes.cinemaPlus.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record MovieDto (
        @NotNull(message = "Название не может быть пустым")
        String title,

        @Size(min = 100, max = 2000, message = "Описание не должно быть менее 100 символов")
        String description,

        @Min(value = 10, message = "Длительность фильма должна быть не менее 10 минут")
        @Max(value = 300, message = "Длительность фильма должна быть не более 300 минут")
        @NotNull(message = "Длительность не может быть пустым")
        Integer durationMinutes,

        @URL(message = "Некорректный URL адрес")
        @NotNull(message = "Url не может быть пустым")
        String posterUrl
) {}

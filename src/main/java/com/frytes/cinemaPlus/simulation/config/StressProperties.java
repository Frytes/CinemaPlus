package com.frytes.cinemaPlus.simulation.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cinema.stress")
public record StressProperties(
        boolean enabled,

        @NotBlank(message = "Target URL must not be empty")
        String targetUrl,

        @Min(value = 1, message = "At least 1 bot required")
        @Max(value = 10000, message = "Too many bots! Max 10000 to prevent OOM")
        int botCount,

        @Min(value = 1, message = "Ramp up must be at least 1 second")
        int rampUpSeconds,

        @Min(value = 0, message = "Think time cannot be negative")
        int thinkTimeMs,

        @Min(0)
        @Max(100)
        int buyProbability,

        @Min(1)
        int daysToScan
) {}
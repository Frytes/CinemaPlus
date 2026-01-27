package com.frytes.cinemaPlus.booking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardStatsDto(
        BigDecimal revenueToday,
        Long ticketsToday,
        BigDecimal revenueMonth,
        Long totalUsers,
        List<MovieStatDto> topMovies,
        Map<String, BigDecimal> dailyRevenue
) {}

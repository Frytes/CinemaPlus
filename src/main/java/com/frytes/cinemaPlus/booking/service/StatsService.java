package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.dto.DashboardStatsDto;
import com.frytes.cinemaPlus.booking.dto.MovieStatDto;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 1. Карточки
        BigDecimal revenueToday = orderRepository.countRevenueBetween(startOfDay, endOfDay);
        Long ticketsToday = orderRepository.countTicketsBetween(startOfDay, endOfDay);
        BigDecimal revenueMonth = orderRepository.countRevenueBetween(startOfMonth, endOfDay);
        Long totalUsers = userRepository.count();

        // 2. Топ 5 фильмов
        List<MovieStatDto> topMovies = orderRepository.findTopMoviesByRevenue(
                OrderStatus.PAID,
                PageRequest.of(0, 5)
        );
        // 3. График за 7 дней (Daily Revenue)
        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime s = date.atStartOfDay();
            LocalDateTime e = date.atTime(23, 59, 59);
            BigDecimal val = orderRepository.countRevenueBetween(s, e);
            dailyRevenue.put(date.format(DateTimeFormatter.ofPattern("dd.MM")), val);
        }

        return new DashboardStatsDto(revenueToday, ticketsToday, revenueMonth, totalUsers, topMovies, dailyRevenue);
    }
}
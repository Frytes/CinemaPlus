package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCleanupService {

    private final OrderRepository orderRepository;
    private final BookingService bookingService;

    @Value("${cinema.rules.lock-duration-minutes:10}")
    private int lockDurationMinutes;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(lockDurationMinutes);

        List<Order> expiredOrders = orderRepository.findAllByStatusAndCreatedAtBefore(
                OrderStatus.PENDING,
                threshold);

        for (Order order : expiredOrders) {
            log.info("🗑️ Отмена просроченного заказа #{}", order.getId());
            try {
                bookingService.cancelBooking(order.getId(), order.getUser());
            } catch (Exception e) {
                log.error("❌ Ошибка при отмене заказа #{}", order.getId(), e);
            }
        }
    }
}
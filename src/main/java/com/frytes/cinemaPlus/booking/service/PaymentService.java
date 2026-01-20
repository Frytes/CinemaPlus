package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final Random random = new Random();

    @Value("${cinema.payment.delay-ms:1000}")
    private int delayMs;

    @Value("${cinema.payment.fail-probability:10}")
    private int failProbability;

    @SneakyThrows
    public boolean processPayment(Long orderId) {

        if (delayMs > 0) {
            Thread.sleep(delayMs + random.nextInt(200));
        }

        boolean success = random.nextInt(100) >= failProbability;

        return finalizeOrder(orderId, success);
    }

    @Transactional
    protected boolean finalizeOrder(Long orderId, boolean paymentSuccess) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        if (paymentSuccess) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            return true;
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return false;
        }
    }
}
package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import com.frytes.cinemaPlus.users.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final HtmlTemplateService templateService;

    @KafkaListener(topics = "booking-events-topic", groupId = "notification-group")
    public void handleBookingPaid(BookingPaidEvent event) {
        log.info("📩 Получено событие об оплате: Order ID {}", event.orderId());

        String html = templateService.createBookingConfirmation(event);
        emailService.sendHtmlEmail(event.userEmail(), "Ваш билет в CinemaPlus! 🍿", html);
    }

    @KafkaListener(topics = "user-events-topic", groupId = "notification-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailService.sendHtmlEmail(event.email(), "Добро пожаловать!", "<h1>Привет!</h1>");
    }
}
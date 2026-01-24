package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import com.frytes.cinemaPlus.common.service.QrCodeService;
import com.frytes.cinemaPlus.users.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final HtmlTemplateService templateService;
    private final QrCodeService qrCodeService;

    @KafkaListener(topics = "booking-events-topic", groupId = "notification-group")
    public void handleBookingPaid(BookingPaidEvent event) {
        log.info("📩 Генерация письма для заказа #{}", event.orderId());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<Map<String, String>> seatsInfo = event.tickets().stream()
                .map(t -> Map.of(
                        "row", String.valueOf(t.rowIndex() + 1),
                        "seat", t.seatNumber(),
                        "type", t.type().name()
                ))
                .toList();

        Map<String, Object> qrData = Map.of(
                "order_id", event.orderId(),
                "movie", event.movieTitle(),
                "date", event.eventTime().format(dateFormatter),
                "time", event.eventTime().format(timeFormatter),
                "tickets", seatsInfo
        );

        byte[] qrImageBytes = qrCodeService.generateQrCodeFromData(qrData);

        String qrBase64 = Base64.getEncoder().encodeToString(qrImageBytes);

        String html = templateService.createBookingConfirmation(event, qrBase64);

        emailService.sendHtmlEmail(
                event.userEmail(),
                "Ваш билет в CinemaPlus! 🍿",
                html
        );
    }

    @KafkaListener(topics = "user-events-topic", groupId = "notification-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        String html = String.format("<h1>Добро пожаловать, %s!</h1><p>Регистрация прошла успешно.</p>", event.username());
        emailService.sendHtmlEmail(event.email(), "Добро пожаловать в CinemaPlus! 👋", html);
    }
}
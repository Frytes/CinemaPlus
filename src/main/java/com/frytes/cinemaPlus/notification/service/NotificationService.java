package com.frytes.cinemaPlus.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "booking-events-topic",
            groupId = "notification-group"
    )
    public void handleBookingPaid(String message) {
        log.info("📨 Kafka Consumer received event: {}", message);
        try{
            BookingPaidEvent event = objectMapper.readValue(message, BookingPaidEvent.class);
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
        } catch (Exception e) {
            log.error("❌ Ошибка обработки сообщения для отправки сообщения на почту: {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "user-events-topic", groupId = "notification-group", concurrency = "20")
    public void handleUserRegistered(String message) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);

            log.info("👤 Обработка регистрации пользователя: {}", event.email());

            String html = templateService.createWelcomeEmail(event);

            emailService.sendHtmlEmail(
                    event.email(),
                    "Добро пожаловать в CinemaPlus! 🎬",
                    html
            );
            Thread.sleep(50);

            log.info("✅ Приветственное письмо отправлено на: {}", event.email());

        } catch (Exception e) {
            log.error("❌ Ошибка отправки приветственного письма: {}", e.getMessage());
        }
    }
}
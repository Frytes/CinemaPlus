package com.frytes.cinemaPlus.notification;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.OutboxEvent;
import com.frytes.cinemaPlus.booking.entity.enumps.OutboxStatus;
import com.frytes.cinemaPlus.booking.repository.OutboxRepository;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.service.BookingService;
import com.frytes.cinemaPlus.booking.service.PaymentService;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.repository.HallRepository;
import com.frytes.cinemaPlus.repository.MovieRepository;
import com.frytes.cinemaPlus.repository.SeatRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@DisplayName("📧 Интеграционный тест асинхронных уведомлений")
class AsyncNotificationIntegrationTest extends BaseIntegrationTest {

    @Autowired private PaymentService paymentService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private BookingService bookingService;
    @Autowired private OutboxRepository outboxRepository; // Добавьте этот репозиторий

    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUpMocks() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("После успешной оплаты должно отправиться письмо (через Kafka)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldSendEmail_WhenOrderIsPaid() {
        log.info("=== НАЧАЛО ТЕСТА: shouldSendEmail_WhenOrderIsPaid ===");

        //GIVEN
        log.info("1. Создаем тестовые данные...");

        User user = new User(null, "testuser", "test@email.com", "password123", Role.USER);
        Hall hall = new Hall(null, "Test Hall", 5, 5, new ArrayList<>());
        Seat seat = new Seat(null, hall, 1, 1, SeatType.STANDARD, "A1");
        hall.addSeat(seat);
        Movie movie = new Movie(null, "Test Movie",
                "Это очень длинное описание фильма, которое должно быть больше 100 символов, чтобы пройти проверку базы данных Postgres. Надеюсь, этого текста достаточно для теста.",
                120, "url", 2024, 8.0, 12, "Genre");
        Session session = new Session(null, movie, hall,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                BigDecimal.valueOf(100));

        userRepository.save(user);
        hallRepository.save(hall);
        movieRepository.save(movie);
        sessionRepository.save(session);

        log.info("   ✅ Данные созданы: user={}, session={}, seat={}",
                user.getEmail(), session.getId(), seat.getId());

        // Проверяем outbox перед бронированием
        List<OutboxEvent> initialOutbox = outboxRepository.findAll();
        log.info("   📊 Outbox перед бронированием: {} событий", initialOutbox.size());

        BookingRequest bookingRequest = new BookingRequest(session.getId(), List.of(seat.getId()));
        Order order = bookingService.createBooking(bookingRequest, user);
        Long orderId = order.getId();

        log.info("2. Заказ создан: orderId={}, status={}, totalPrice={}",
                orderId, order.getStatus(), order.getTotalPrice());

        // Проверяем outbox после бронирования
        List<OutboxEvent> afterBookingOutbox = outboxRepository.findAll();
        log.info("   📊 Outbox после бронирования: {} событий", afterBookingOutbox.size());
        afterBookingOutbox.forEach(e ->
                log.info("     - {}: {} (status={})", e.getId(), e.getTopic(), e.getStatus()));

        // Проверяем статус заказа перед оплатой
        Order orderBeforePayment = orderRepository.findById(orderId).orElseThrow();
        log.info("3. Перед оплатой: order status={}", orderBeforePayment.getStatus());

        // WHEN
        log.info("4. Вызываем paymentService.processPayment({})...", orderId);
        paymentService.processPayment(orderId);

        log.info("5. Проверяем статус заказа после оплаты...");
        Order orderAfterPayment = orderRepository.findById(orderId).orElseThrow();
        log.info("   ✅ Статус заказа после paymentService: {}", orderAfterPayment.getStatus());

        // Проверяем outbox сразу после оплаты
        List<OutboxEvent> afterPaymentOutbox = outboxRepository.findAll();
        log.info("   📊 Outbox после оплаты: {} событий", afterPaymentOutbox.size());

        if (afterPaymentOutbox.isEmpty()) {
            log.error("   ❌ ВАЖНО: Outbox пуст после оплаты! Возможные причины:");
            log.error("      - BookingPaidEvent не создается в PaymentService");
            log.error("      - OutboxEvent не сохраняется в outbox таблицу");
            log.error("      - Транзакция не фиксируется (проверьте @Transactional)");
        } else {
            afterPaymentOutbox.forEach(e -> {
                log.info("     - Outbox #{}: topic={}, status={}, created={}",
                        e.getId(), e.getTopic(), e.getStatus(), e.getCreatedAt());

                // Если есть новое событие для booking
                if (e.getStatus() == OutboxStatus.NEW && e.getTopic().contains("booking")) {
                    String payloadPreview = e.getPayload().length() > 100
                            ? e.getPayload().substring(0, 100) + "..."
                            : e.getPayload();
                    log.info("       📄 Payload preview: {}", payloadPreview);
                }
            });
        }

        // Ждем и проверяем, обрабатывает ли scheduler outbox
        log.info("6. Ждем обработки outbox scheduler...");
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<OutboxEvent> currentOutbox = outboxRepository.findAll();
            long newCount = currentOutbox.stream()
                    .filter(e -> e.getStatus() == OutboxStatus.NEW)
                    .count();
            long sentCount = currentOutbox.stream()
                    .filter(e -> e.getStatus() == OutboxStatus.SENT)
                    .count();

            log.info("   ⏱️ Секунда {}: NEW={}, SENT={}", i + 1, newCount, sentCount);

            if (sentCount > 0) {
                log.info("   ✅ Scheduler обработал событие!");
                break;
            }
        }

        // THEN
        log.info("7. Проверяем отправку email...");

        try {
            await()
                    .atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(() -> {
                        log.info("   🔍 Проверка вызова JavaMailSender...");

                        // Проверяем текущее состояние outbox перед проверкой email
                        List<OutboxEvent> currentEvents = outboxRepository.findAll();
                        log.info("   📊 Текущий outbox: {} событий", currentEvents.size());
                        currentEvents.forEach(e ->
                                log.info("     - {}: {} ({})", e.getId(), e.getTopic(), e.getStatus()));

                        verify(javaMailSender).send(any(MimeMessage.class));
                        log.info("   ✅ JavaMailSender.send() был вызван!");
                    });

            log.info("🎉 ТЕСТ ПРОЙДЕН УСПЕШНО!");

        } catch (org.awaitility.core.ConditionTimeoutException e) {
            log.error("❌ ТАЙМАУТ: JavaMailSender.send() не был вызван за 15 секунд");
            log.error("Диагностика проблемы:");


            List<OutboxEvent> finalOutbox = outboxRepository.findAll();
            log.error("Финальный outbox ({} событий):", finalOutbox.size());
            finalOutbox.forEach(event ->
                    log.error("   - {}: {} (status={})",
                            event.getId(), event.getTopic(), event.getStatus()));


            Order finalOrder = orderRepository.findById(orderId).orElseThrow();
            log.error("Финальный статус заказа #{}: {}", orderId, finalOrder.getStatus());


            int mockCallCount = 0;
            try {
                java.lang.reflect.Field field = org.mockito.internal.MockitoCore.class.getDeclaredField("mockitoCore");
                field.setAccessible(true);
                Object mockitoCore = field.get(null);
                // Логика для подсчета вызовов...
            } catch (Exception ex) {
                // Игнорируем
            }
            log.error("Количество вызовов JavaMailSender: {}", mockCallCount);

            throw e;
        }

        log.info("=== КОНЕЦ ТЕСТА ===");
    }
}
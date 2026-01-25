package com.frytes.cinemaPlus.booking;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.booking.service.BookingCleanupService;
import com.frytes.cinemaPlus.booking.service.BookingLockService;
import com.frytes.cinemaPlus.booking.service.BookingService;
import com.frytes.cinemaPlus.booking.service.PaymentService;
import com.frytes.cinemaPlus.content.dto.enums.SocketStatus;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.notification.service.SocketNotificationService;
import com.frytes.cinemaPlus.repository.HallRepository;
import com.frytes.cinemaPlus.repository.MovieRepository;
import com.frytes.cinemaPlus.repository.SeatRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("🛡️ Тесты надежности и идемпотентности")
class BookingReliabilityTest extends BaseIntegrationTest {

    @Autowired private BookingService bookingService;
    @Autowired private PaymentService paymentService;
    @Autowired private BookingLockService bookingLockService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private BookingCleanupService bookingCleanupService;

    @Autowired private UserRepository userRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private SeatRepository seatRepository;

    @MockitoBean
    private SocketNotificationService socketNotificationService;

    private User user;
    private Session session;
    private Seat seat;

    @Value("${cinema.rules.lock-duration-minutes}")
    private int lockDurationMinutes;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        sessionRepository.deleteAll();
        hallRepository.deleteAll();
        movieRepository.deleteAll();

        paymentService.setFailProbability(0);
        paymentService.setDelayMs(0);

        user = userRepository.save(new User(null, "reliabilityUser", "rel@test.com", "password123", Role.USER));

        Movie movie = movieRepository.save(new Movie(null, "Test Movie", "Это очень длинное описание фильма, которое должно быть больше 100 символов, чтобы пройти проверку базы данных Postgres. Надеюсь, этого текста достаточно для теста.", 120, "url", 2024, 8.0, 12, "Genre"));
        Hall hall = new Hall(null, "Reliability Hall", 5, 5, new ArrayList<>());
        seat = new Seat(null, hall, 1, 1, SeatType.STANDARD, "A1");

        hallRepository.save(hall);
        hall.addSeat(seat);
        seatRepository.save(seat);

        session = sessionRepository.save(new Session(null, movie, hall, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4), BigDecimal.valueOf(300)));
    }

    @Test
    @DisplayName("💳 Платеж должен быть идемпотентным (повторный вызов не меняет статус)")
    void shouldBeIdempotent_WhenPaymentProcessedTwice() {
        BookingRequest request = new BookingRequest(session.getId(), List.of(seat.getId()));
        Order order = bookingService.createBooking(request, user);
        Long orderId = order.getId();

        boolean firstAttempt = paymentService.processPayment(orderId);

        boolean secondAttempt = paymentService.processPayment(orderId);

        assertThat(firstAttempt).as("Первая оплата должна пройти").isTrue();
        assertThat(secondAttempt).as("Вторая оплата должна быть отклонена (уже оплачено)").isFalse();

        Order finalOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        verify(socketNotificationService, times(1))
                .sendSeatUpdate(eq(session.getId()), any(), eq(SocketStatus.SOLD));
    }


    @Test
    @DisplayName("🧹 Отмена заказа должна снимать лок в Redis и слать сокет-уведомление")
    void shouldCleanUpResources_WhenBookingCancelled() {
        BookingRequest request = new BookingRequest(session.getId(), List.of(seat.getId()));
        Order order = bookingService.createBooking(request, user);

        String redisKey = String.format("booking:session:%d:seat:%d", session.getId(), seat.getId());
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();

        bookingService.cancelBooking(order.getId(), user);

        assertThat(redisTemplate.hasKey(redisKey)).as("Redis лок должен быть удален").isFalse();

        Order cancelledOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(socketNotificationService).sendSeatUpdate(eq(session.getId()), any(), eq(SocketStatus.AVAILABLE));
    }


    @Test
    @DisplayName("⏳ Просроченная бронь должна быть очищена, а место освобождено")
    void shouldCleanup_WhenBookingExpired() {
        BookingRequest request1 = new BookingRequest(session.getId(), List.of(seat.getId()));
        Order order = bookingService.createBooking(request1, user);

        LocalDateTime oldDate = LocalDateTime.now().minusMinutes(lockDurationMinutes + 5);
        orderRepository.forceUpdateCreatedAt(order.getId(), oldDate);

        bookingCleanupService.cleanupExpiredBookings();

        Order expiredOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);


        User user2 = userRepository.save(new User(null, "user2", "u2@test.com", "password123", Role.USER));
        Order order2 = bookingService.createBooking(request1, user2);

        assertThat(order2).isNotNull();
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}

package com.frytes.cinemaPlus.notification;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.entity.Order;
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
import static org.mockito.Mockito.*;

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

    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUpMocks() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        paymentService.setFailProbability(0);
        paymentService.setDelayMs(0);
    }

    @Test
    @DisplayName("После успешной оплаты должно отправиться письмо (через Kafka)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldSendEmail_WhenOrderIsPaid() {
        //GIVEN
        User user = new User(null, "testuser", "test@email.com", "password123", Role.USER);
        Hall hall = new Hall(null, "Test Hall", 5, 5, new ArrayList<>());
        Seat seat = new Seat(null, hall, 1, 1, SeatType.STANDARD, "A1");
        hall.addSeat(seat);
        Movie movie = new Movie(null, "Test Movie",  "Это очень длинное описание фильма, которое должно быть больше 100 символов, чтобы пройти проверку базы данных Postgres. Надеюсь, этого текста достаточно для теста.", 120, "url", 2024, 8.0, 12, "Genre");
        Session session = new Session(null, movie, hall, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal.valueOf(100));

        userRepository.save(user);
        hallRepository.save(hall);
        movieRepository.save(movie);
        sessionRepository.save(session);

        BookingRequest bookingRequest = new BookingRequest(session.getId(), List.of(seat.getId()));
        Order order = bookingService.createBooking(bookingRequest, user);
        Long orderId = order.getId();

        // WHEN
        paymentService.processPayment(orderId);

        // THEN
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> verify(javaMailSender).send(any(MimeMessage.class)));
    }
}
package com.frytes.cinemaPlus.notification;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.Ticket;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("📧 Интеграционный тест асинхронных уведомлений")
class AsyncNotificationIntegrationTest extends BaseIntegrationTest {

    @Autowired private PaymentService paymentService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private SeatRepository seatRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUpMocks() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("После успешной оплаты должно отправиться письмо (через Kafka)")
    void shouldSendEmail_WhenOrderIsPaid() {
        //GIVEN
        User user = new User(null, "testuser", "test@email.com", "password123", Role.USER);
        userRepository.save(user);

        Hall hall = new Hall(null, "Test Hall", 5, 5, new ArrayList<>());
        Seat seat = new Seat(null, hall, 1, 1, SeatType.STANDARD, "A1");
        hall.addSeat(seat);
        hallRepository.save(hall);

        Movie movie = new Movie(null, "Test Movie",  "Это очень длинное описание фильма, которое должно быть больше 100 символов, чтобы пройти проверку базы данных Postgres. Надеюсь, этого текста достаточно для теста.", 120, "url", 2024, 8.0, 12, "Genre");
        movieRepository.save(movie);

        Session session = new Session(null, movie, hall, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal.valueOf(100));
        sessionRepository.save(session);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(100))
                .tickets(new ArrayList<>())
                .build();

        Ticket ticket = Ticket.builder()
                .session(session)
                .seat(seat)
                .order(order)
                .build();
        order.getTickets().add(ticket);

        orderRepository.save(order);

        // WHEN
        paymentService.processPayment(order.getId());

        // THEN
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> verify(javaMailSender).send(any(MimeMessage.class)));
    }
}
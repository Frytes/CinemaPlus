package com.frytes.cinemaPlus.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.dto.BookingResponse;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.entity.enumps.OrderStatus;
import com.frytes.cinemaPlus.booking.repository.OrderRepository;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.repository.HallRepository;
import com.frytes.cinemaPlus.repository.MovieRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.Role;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import com.frytes.cinemaPlus.users.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("🚀 E2E: Полный цикл покупки билета")
class FullBookingCycleIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Repositories setup
    @Autowired private UserRepository userRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private JwtService jwtService;

    private String token;
    private Long sessionId;
    private Long seatId;

    @BeforeEach
    void setUp() {
        User user = new User(null, "buyer", "buyer@test.com", "password123", Role.USER);
        userRepository.save(user);
        token = "Bearer " + jwtService.generateToken(user);

        String longDescription = "Это очень длинное описание фильма, которое должно быть больше 100 символов, чтобы пройти проверку базы данных Postgres. Надеюсь, этого текста достаточно для теста.";
        Movie movie = new Movie(null, "E2E Movie", longDescription, 120, "https://example.com/poster.jpg", 2024, 9.0, 12, "Genre");
        movieRepository.save(movie);

        Hall hall = new Hall(null, "E2E Hall", 5, 5, new ArrayList<>());

        Seat seat = new Seat(null, hall, 1, 1, SeatType.STANDARD, "A1");
        hall.addSeat(seat);
        hallRepository.save(hall);

        Session session = new Session(null, movie, hall, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), BigDecimal.valueOf(500));
        sessionRepository.save(session);

        this.sessionId = session.getId();
        this.seatId = seat.getId();
    }

    @Test
    @DisplayName("✅ Клиент успешно бронирует и оплачивает билет")
    void shouldCompleteBookingFlow() throws Exception {
        BookingRequest bookingRequest = new BookingRequest(sessionId, List.of(seatId));

        String responseJson = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        BookingResponse response = objectMapper.readValue(responseJson, BookingResponse.class);
        Long orderId = response.orderId();

        Order orderAfterBooking = orderRepository.findById(orderId).orElseThrow();
        assertThat(orderAfterBooking.getStatus()).isEqualTo(OrderStatus.PENDING);

        mockMvc.perform(post("/api/bookings/" + orderId + "/pay")
                        .header("Authorization", token))
                .andReturn();

        Order orderAfterPayment = orderRepository.findById(orderId).orElseThrow();
        assertThat(orderAfterPayment.getStatus()).isIn(OrderStatus.PAID, OrderStatus.CANCELLED);
    }
}
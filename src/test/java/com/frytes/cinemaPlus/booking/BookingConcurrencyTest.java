package com.frytes.cinemaPlus.booking;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.service.BookingService;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("🔥 Тест на конкурентное бронирование")
class BookingConcurrencyTest extends BaseIntegrationTest {

    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private SessionRepository sessionRepository;

    @Test
    @DisplayName("⚔️ Тест на избежание двойного бронирования (Race Condition)")
    void shouldPreventDoubleBooking_WhenMultipleUsersTryToBuySameSeat() throws InterruptedException {
        Hall hall = new Hall(null, "Race Hall", 10, 10, new ArrayList<>());
        Seat seat = new Seat(null, hall, 1, 1, SeatType.STANDARD, "A1");
        hall.addSeat(seat);
        hallRepository.save(hall);

        String longDescription = "Это очень длинное описание фильма, которое должно быть больше 100 символов, чтобы пройти проверку базы данных Postgres. Надеюсь, этого текста достаточно для теста.";
        Movie movie = new Movie(null, "Race Movie", longDescription, 120, "url", 2024, 9.0, 12, "Genre");
        movieRepository.save(movie);

        Session session = new Session(null, movie, hall, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal.valueOf(100));
        sessionRepository.save(session);

        Long sessionId = session.getId();
        Long seatId = seat.getId();

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            userRepository.save(new User(null, "racer" + i, "race" + i + "@test.com", "pass", Role.USER));
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    User user = userRepository.findByEmail("race" + finalI + "@test.com").orElseThrow();
                    BookingRequest request = new BookingRequest(sessionId, List.of(seatId));
                    bookingService.createBooking(request, user);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("Booking failed for user " + finalI + ": " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(successCount.get()).as("Только один пользователь должен выполнить операцию успешно").isEqualTo(1);
        assertThat(failCount.get()).as("Остальные пользователи должны получить ошибку").isEqualTo(threads - 1);
    }
}
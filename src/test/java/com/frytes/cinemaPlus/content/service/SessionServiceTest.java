package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.content.dto.SessionRequest;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.repository.HallRepository;
import com.frytes.cinemaPlus.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionServiceTest extends BaseIntegrationTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    @Test
    void shouldThrowException_WhenSessionsOverlap() {
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setDurationMinutes(120);
        movie.setPosterUrl("http://url");
        movieRepository.save(movie);

        Hall hall = new Hall();
        hall.setName("Test Hall");
        hall.setWidth(10);
        hall.setHeight(10);
        hallRepository.save(hall);


        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);

        SessionRequest request1 = new SessionRequest(
                movie.getId(),
                hall.getId(),
                start1,
                BigDecimal.valueOf(500),
                10,
                20
        );
        sessionService.createSession(request1);

        LocalDateTime start2 = LocalDateTime.now().plusDays(1).withHour(19).withMinute(30);

        SessionRequest request2 = new SessionRequest(
                movie.getId(),
                hall.getId(),
                start2,
                BigDecimal.valueOf(500),
                10,
                20
        );

        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.createSession(request2);
        });
    }
}
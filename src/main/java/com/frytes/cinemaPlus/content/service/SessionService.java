package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.booking.repository.TicketRepository;
import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.SessionDto;
import com.frytes.cinemaPlus.content.dto.SessionMapper;
import com.frytes.cinemaPlus.content.dto.SessionRequest;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.repository.HallRepository;
import com.frytes.cinemaPlus.content.repository.MovieRepository;
import com.frytes.cinemaPlus.content.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SessionMapper sessionMapper;
    private final TicketRepository ticketRepository;

    @Value("${cinema.rules.default-ads-minutes:15}")
    private int defaultAds;

    @Value("${cinema.rules.default-cleanup-minutes:20}")
    private int defaultCleanup;

    @Transactional
    public void createSession(SessionRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("Фильм не найден"));
        Hall hall = hallRepository.findById(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("Зал не найден"));


        int ads = request.adsMinutes() != null ? request.adsMinutes() : defaultAds;
        int cleanup = request.cleanupMinutes() != null ? request.cleanupMinutes() : defaultCleanup;

        LocalDateTime start = request.startTime();

        LocalDateTime end = start
                .plusMinutes(ads)
                .plusMinutes(movie.getDurationMinutes())
                .plusMinutes(cleanup);

        boolean overlap = sessionRepository.existsByHallIdAndStartTimeBeforeAndEndTimeAfter(
                hall.getId(), end, start
        );

        if (overlap) {
            throw new IllegalArgumentException("В это время зал занят другим сеансом!");
        }

        Session session = Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(start)
                .endTime(end)
                .basePrice(request.basePrice())
                .build();

        sessionRepository.save(session);
    }
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return sessionRepository.findByStartTimeBetween(startOfDay, endOfDay)
                .stream()
                .map(sessionMapper::toDto)
                .toList();
    }

    public List<SessionDto> getSessionsByMovie(Long movieId) {
        return sessionRepository.findByMovieIdAndStartTimeAfter(movieId, LocalDateTime.now())
                .stream()
                .map(sessionMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Сеанс не найден");
        }
        if (ticketRepository.existsBySessionId(id)) {
            throw new IllegalStateException("Нельзя удалить сеанс, на который уже куплены билеты");
        }
        sessionRepository.deleteById(id);
    }

    @Transactional
    public void updateSession(Long id, SessionRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сеанс не найден"));

        boolean hasTickets = ticketRepository.existsBySessionId(id);

        boolean isCriticalChange = !session.getMovie().getId().equals(request.movieId()) ||
                !session.getHall().getId().equals(request.hallId()) ||
                !session.getStartTime().isEqual(request.startTime());

        if (hasTickets && isCriticalChange) {
            throw new IllegalStateException("Нельзя менять фильм, зал или время, если уже проданы билеты! Можно менять только цену.");
        }


        if (isCriticalChange) {
            Movie movie = movieRepository.findById(request.movieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Фильм не найден"));

            int ads = request.adsMinutes() != null ? request.adsMinutes() : defaultAds;
            int cleanup = request.cleanupMinutes() != null ? request.cleanupMinutes() : defaultCleanup;

            LocalDateTime end = request.startTime()
                    .plusMinutes(ads)
                    .plusMinutes(movie.getDurationMinutes())
                    .plusMinutes(cleanup);

            boolean overlap = sessionRepository.existsByHallIdAndStartTimeBeforeAndEndTimeAfterAndIdNot(
                    request.hallId(), end, request.startTime(), id
            );

            if (overlap) {
                throw new IllegalArgumentException("В это время зал занят другим сеансом!");
            }

            session.setMovie(movie);
            Hall hall = hallRepository.findById(request.hallId())
                    .orElseThrow(() -> new ResourceNotFoundException("Зал не найден"));
            session.setHall(hall);
            session.setStartTime(request.startTime());
            session.setEndTime(end);
        }

        session.setBasePrice(request.basePrice());

        sessionRepository.save(session);
    }
}
package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.SessionDto;
import com.frytes.cinemaPlus.content.dto.SessionMapper;
import com.frytes.cinemaPlus.content.dto.SessionRequest;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.repository.HallRepository;
import com.frytes.cinemaPlus.repository.MovieRepository;
import com.frytes.cinemaPlus.repository.SessionRepository;
import com.frytes.cinemaPlus.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SessionMapper sessionMapper;

    @Value("${cinema.default-ads-minutes:15}")
    private int defaultAds;

    @Value("${cinema.default-cleanup-minutes:20}")
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

        Session session = new Session();
        session.setMovie(movie);
        session.setHall(hall);
        session.setStartTime(start);
        session.setEndTime(end);
        session.setBasePrice(request.basePrice());

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
}
package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.content.dto.MovieDto;
import com.frytes.cinemaPlus.content.dto.MovieMapper;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Transactional
    public void createMovie(MovieDto dto) {
        log.info("Creating movie: {}", dto.title());
        Movie movie = movieMapper.toEntity(dto);
        movieRepository.save(movie);
        log.info("Movie created with ID: {}", movie.getId());
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movieMapper::toDto)
                .toList();
    }
}
package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.MovieDto;
import com.frytes.cinemaPlus.content.dto.MovieMapper;
import com.frytes.cinemaPlus.content.entity.Movie;
import com.frytes.cinemaPlus.content.repository.MovieRepository;
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

    public MovieDto getMovieById(Long id){
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм не найден с id: " + id));
        return movieMapper.toDto(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movieMapper::toDto)
                .toList();
    }

    @Transactional
    public void updateMovie(Long id, MovieDto dto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм не найден"));

        movieMapper.updateMovieFromDto(dto, movie);

        movieRepository.save(movie);
        log.info("Movie updated: {}", id);
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Фильм не найден");
        }
        movieRepository.deleteById(id);
        log.info("Movie deleted: {}", id);
    }
}
package com.frytes.cinemaPlus.content.controller;

import com.frytes.cinemaPlus.content.dto.MovieDto;
import com.frytes.cinemaPlus.content.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieDto>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PostMapping
    public ResponseEntity<Void> createMovie(@Valid @RequestBody MovieDto dto) {
        movieService.createMovie(dto);
        return ResponseEntity.status(201).build();
    }
}
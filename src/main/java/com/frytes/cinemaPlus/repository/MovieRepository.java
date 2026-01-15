package com.frytes.cinemaPlus.repository;

import com.frytes.cinemaPlus.content.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}

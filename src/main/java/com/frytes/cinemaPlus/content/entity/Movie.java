package com.frytes.cinemaPlus.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "poster_url", nullable = false)
    private String posterUrl;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "age_limit")
    private Integer ageLimit;

    @Column(name = "genre")
    private String genre;
}
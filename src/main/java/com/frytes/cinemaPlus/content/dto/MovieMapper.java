package com.frytes.cinemaPlus.content.dto;

import com.frytes.cinemaPlus.content.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {
    @Mapping(target = "id", ignore = true)
    Movie toEntity(MovieDto dto);

    MovieDto toDto(Movie entity);

    @Mapping(target = "id", ignore = true)
    void updateMovieFromDto(MovieDto dto, @MappingTarget Movie movie);
}
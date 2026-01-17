package com.frytes.cinemaPlus.content.dto;

import com.frytes.cinemaPlus.content.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "hallId", source = "hall.id")
    @Mapping(target = "hallName", source = "hall.name")
    @Mapping(target = "price", source = "basePrice")
    SessionDto toDto(Session session);
}

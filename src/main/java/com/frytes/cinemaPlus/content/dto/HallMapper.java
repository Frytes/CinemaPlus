package com.frytes.cinemaPlus.content.dto;

import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HallMapper {
    HallSummaryDto toSummary(Hall hall);
    HallDetailDto toDetail(Hall hall);
    SeatDto toSeatDto(Seat seat);
}

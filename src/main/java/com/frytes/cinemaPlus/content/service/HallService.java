package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.*;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final HallMapper hallMapper;

    @Transactional
    public void createHall(HallRequest request) {
        Hall hall = new Hall();
        hall.setName(request.name());
        hall.setWidth(request.width());
        hall.setHeight(request.height());
        for (int row = 0; row < request.height(); row++) {
            for (int col = 0; col < request.width(); col++) {
                Seat seat = new Seat();
                seat.setRowIndex(row);
                seat.setColIndex(col);
                seat.setType(SeatType.STANDARD);
                String seatNum = String.format("%d-%d", row + 1, col + 1);
                seat.setSeatNumber(seatNum);
                hall.addSeat(seat);
            }
        }
        hallRepository.save(hall);
    }
    @Transactional(readOnly = true)
    public HallDetailDto getHallById(Long id){
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Зал не найден"));
        return hallMapper.toDetail(hall);
    }
    @Transactional(readOnly = true)
    public List<HallSummaryDto> getAllHalls(){
        return hallRepository.findAll().stream()
                .map(hallMapper::toSummary)
                .toList();
    }
}

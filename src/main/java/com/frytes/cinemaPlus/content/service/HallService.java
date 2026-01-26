package com.frytes.cinemaPlus.content.service;

import com.frytes.cinemaPlus.common.exception.ResourceNotFoundException;
import com.frytes.cinemaPlus.content.dto.HallDetailDto;
import com.frytes.cinemaPlus.content.dto.HallMapper;
import com.frytes.cinemaPlus.content.dto.HallRequest;
import com.frytes.cinemaPlus.content.dto.HallSummaryDto;
import com.frytes.cinemaPlus.content.entity.Hall;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import com.frytes.cinemaPlus.content.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        if (request.seats() != null && !request.seats().isEmpty()) {
            for (HallRequest.SeatConfigDto seatConf : request.seats()) {
                Seat seat = Seat.builder()
                        .hall(hall)
                        .rowIndex(seatConf.row())
                        .colIndex(seatConf.col())
                        .type(seatConf.type())
                        .seatNumber(seatConf.seatNumber())
                        .build();
                hall.addSeat(seat);
            }
        } else {
            for (int row = 0; row < request.height(); row++) {
                for (int col = 0; col < request.width(); col++) {
                    Seat seat = Seat.builder()
                            .hall(hall)
                            .rowIndex(row)
                            .colIndex(col)
                            .type(SeatType.STANDARD)
                            .seatNumber(String.format("%d-%d", row + 1, col + 1))
                            .build();
                    hall.addSeat(seat);
                }
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

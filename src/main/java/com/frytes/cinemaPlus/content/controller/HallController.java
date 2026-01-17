package com.frytes.cinemaPlus.content.controller;

import com.frytes.cinemaPlus.content.dto.HallDetailDto;
import com.frytes.cinemaPlus.content.dto.HallRequest;
import com.frytes.cinemaPlus.content.dto.HallSummaryDto;
import com.frytes.cinemaPlus.content.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @PostMapping
    public ResponseEntity<Void> createHall(@RequestBody HallRequest request) {
        hallService.createHall(request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDetailDto> getHall(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    @GetMapping
    public ResponseEntity<List<HallSummaryDto>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }
}

package com.frytes.cinemaPlus.content.controller;

import com.frytes.cinemaPlus.content.dto.SessionDto;
import com.frytes.cinemaPlus.content.dto.SessionRequest;
import com.frytes.cinemaPlus.content.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    public final SessionService sessionService;


    @GetMapping
    public ResponseEntity<List<SessionDto>> getAllSessionsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(sessionService.getSessionsByDate(date));
    }

    @PostMapping
    public ResponseEntity<Void> createSession(@Valid @RequestBody SessionRequest request) {
        sessionService.createSession(request);
        return ResponseEntity.status(201).build();
    }
}

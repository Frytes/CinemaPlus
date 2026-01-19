package com.frytes.cinemaPlus.booking.controller;

import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.service.BookingService;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    public final BookingService bookingService;


    @PostMapping
    public ResponseEntity<Void> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User user
    ) {
        bookingService.createBooking(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/session/{sessionId}/seats")
    public ResponseEntity<List<SeatStatusDto>> getSeatsForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getSeatsForSession(sessionId));
    }
}

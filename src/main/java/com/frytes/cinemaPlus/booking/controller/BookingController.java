package com.frytes.cinemaPlus.booking.controller;

import com.frytes.cinemaPlus.booking.dto.BookingMapper;
import com.frytes.cinemaPlus.booking.dto.BookingRequest;
import com.frytes.cinemaPlus.booking.dto.BookingResponse;
import com.frytes.cinemaPlus.booking.dto.PaymentResponse;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.service.BookingService;
import com.frytes.cinemaPlus.booking.service.PaymentService;
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
    public final PaymentService paymentService;
    private final BookingMapper bookingMapper;


    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User user
    ) {
        Order order = bookingService.createBooking(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<PaymentResponse> payOrder(@PathVariable Long orderId) {
        boolean success = paymentService.processPayment(orderId);
        if (success) {
            return ResponseEntity.ok(new PaymentResponse(orderId,"PAID", "Оплата прошла успешна"));
        } else {
            return ResponseEntity.badRequest().body(new PaymentResponse(orderId,"FAILED", "Оплата отклонена банком"));
        }
    }

    @GetMapping("/session/{sessionId}/seats")
    public ResponseEntity<List<SeatStatusDto>> getSeatsForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getSeatsForSession(sessionId));
    }
}

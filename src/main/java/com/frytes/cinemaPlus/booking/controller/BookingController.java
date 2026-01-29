package com.frytes.cinemaPlus.booking.controller;

import com.frytes.cinemaPlus.booking.dto.*;
import com.frytes.cinemaPlus.booking.entity.Order;
import com.frytes.cinemaPlus.booking.service.BookingService;
import com.frytes.cinemaPlus.booking.service.PaymentService;
import com.frytes.cinemaPlus.common.service.QrCodeService;
import com.frytes.cinemaPlus.content.dto.SeatStatusDto;
import com.frytes.cinemaPlus.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    public final BookingService bookingService;
    public final PaymentService paymentService;
    private final BookingMapper bookingMapper;
    private final QrCodeService qrCodeService;

    @GetMapping("/session/{sessionId}/seats")
    public ResponseEntity<List<SeatStatusDto>> getSeatsForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getSeatsForSession(sessionId));
    }

    @GetMapping("/session/{sessionId}/my-pending")
    public ResponseEntity<BookingResponse> getMyPendingBooking(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal User user
    ) {
        return bookingService.findPendingBooking(sessionId, user)
                .map(bookingMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<OrderHistoryDto>> getMyHistory(@AuthenticationPrincipal User user) {
        List<OrderHistoryDto> history = bookingService.getMyOrders(user).stream()
                .map(bookingMapper::toHistoryDto)
                .toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping(value = "/{orderId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getOrderQrCode(@PathVariable Long orderId) {
            Map<String, Object> qrData = bookingService.getOrderQrData(orderId);
            byte[] qrImage = qrCodeService.generateQrCodeFromData(qrData);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrImage);

    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User user
    ) {
        Order order = bookingService.createBooking(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingMapper.toResponse(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long orderId, @AuthenticationPrincipal User user) {
        bookingService.cancelBooking(orderId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<PaymentResponse> payOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        boolean success = paymentService.processPayment(orderId,user);
        if (success) {
            return ResponseEntity.ok(new PaymentResponse(orderId,"PAID", "Оплата прошла успешна"));
        } else {
            return ResponseEntity.badRequest().body(new PaymentResponse(orderId,"FAILED", "Оплата отклонена банком"));
        }
    }
}

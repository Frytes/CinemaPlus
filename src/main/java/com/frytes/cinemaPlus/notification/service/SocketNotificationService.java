package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.content.dto.SocketSeatMessage;
import com.frytes.cinemaPlus.content.dto.enums.SocketStatus;
import com.frytes.cinemaPlus.content.entity.Seat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketNotificationService {

    private final SeatSyncService seatSyncService;

    public void sendSeatUpdate(Long sessionId, Seat seat, SocketStatus status) {
        try {
            SocketSeatMessage message = new SocketSeatMessage(
                    sessionId,
                    seat.getId(),
                    seat.getRowIndex(),
                    seat.getColIndex(),
                    seat.getSeatNumber(),
                    status
            );
            seatSyncService.distribute(message);

            log.debug("📡 Init seat update: Session={} Seat={} Status={}", sessionId, seat.getSeatNumber(), status);
        } catch (Exception e) {
            log.error("❌ Failed to initiate socket update", e);
        }
    }
}
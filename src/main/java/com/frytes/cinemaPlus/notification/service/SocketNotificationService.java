package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.content.dto.SocketSeatMessage;
import com.frytes.cinemaPlus.content.dto.enums.SocketStatus;
import com.frytes.cinemaPlus.content.entity.Seat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

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

            String topic = "/topic/session/" + sessionId;
            messagingTemplate.convertAndSend(topic, message);

            log.debug("📡 Socket sent: Session={} Seat={} Status={}", sessionId, seat.getSeatNumber(), status);
        } catch (Exception e) {
            log.error("❌ Failed to send socket update", e);
        }
    }
}
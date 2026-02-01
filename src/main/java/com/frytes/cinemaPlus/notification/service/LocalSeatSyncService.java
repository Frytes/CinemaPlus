package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.content.dto.SocketSeatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("light")
@RequiredArgsConstructor
public class LocalSeatSyncService implements SeatSyncService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void distribute(SocketSeatMessage message) {
        String topic = "/topic/session/" + message.sessionId();
        messagingTemplate.convertAndSend(topic, message);
    }
}
package com.frytes.cinemaPlus.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frytes.cinemaPlus.common.service.MessageBroker;
import com.frytes.cinemaPlus.content.dto.SocketSeatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Profile("!light")
@RequiredArgsConstructor
public class KafkaSeatSyncService implements SeatSyncService {

    private final MessageBroker messageBroker; // Наш интерфейс!
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void distribute(SocketSeatMessage message) {
        messageBroker.send("seat-updates-topic", String.valueOf(message.sessionId()), message);
    }

    @KafkaListener(
            topics = "seat-updates-topic",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}"
    )
    public void handleBroadcast(String messageJson) {
        try {
            SocketSeatMessage message = objectMapper.readValue(messageJson, SocketSeatMessage.class);

            String topic = "/topic/session/" + message.sessionId();
            messagingTemplate.convertAndSend(topic, message);

        } catch (Exception e) {
            log.error("Error handling broadcast message", e);
        }
    }
}
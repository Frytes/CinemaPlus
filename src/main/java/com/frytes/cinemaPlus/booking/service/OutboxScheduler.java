package com.frytes.cinemaPlus.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frytes.cinemaPlus.booking.entity.OutboxEvent;
import com.frytes.cinemaPlus.booking.entity.enumps.OutboxStatus;
import com.frytes.cinemaPlus.booking.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox(){
        List<OutboxEvent> events = outboxRepository.findAllByStatus(OutboxStatus.NEW);
        for(OutboxEvent event : events){
            try {
                Object payloadObj = objectMapper.readValue(event.getPayload(), Object.class);
                kafkaTemplate.send(event.getTopic(), String.valueOf(event.getId()), payloadObj);
                event.setStatus(OutboxStatus.SENT);

                log.info("✅ Событие ID={} успешно отправлено", event.getId());
            } catch (Exception e) {
                log.error("❌ Ошибка отправки события ID={}: {}", event.getId(), e.getMessage());
            }
        }
        outboxRepository.saveAll(events);
    }
}

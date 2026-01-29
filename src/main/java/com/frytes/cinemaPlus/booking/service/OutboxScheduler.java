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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedDelay = 500)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findAllByStatus(OutboxStatus.NEW);

        if (events.isEmpty()) return;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (OutboxEvent event : events) {
            try {
                Object payloadObj = objectMapper.readValue(event.getPayload(), Object.class);

                var future = kafkaTemplate.send(event.getTopic(), String.valueOf(event.getId()), payloadObj)
                        .thenAccept(result -> {
                            event.setStatus(OutboxStatus.SENT);
                            log.debug("✅ Событие ID={} доставлено", event.getId());
                        })
                        .exceptionally(ex -> {
                            log.error("❌ Ошибка отправки ID={}: {}", event.getId(), ex.getMessage());
                            return null;
                        });

                futures.add(future);

            } catch (Exception e) {
                log.error("💀 Ошибка парсинга ID={}", event.getId());
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        outboxRepository.saveAll(events);

        log.info("🚀 Пачка из {} событий обработана", events.size());
    }
}

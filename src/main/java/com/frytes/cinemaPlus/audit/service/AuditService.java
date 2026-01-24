package com.frytes.cinemaPlus.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frytes.cinemaPlus.audit.entity.AuditLog;
import com.frytes.cinemaPlus.audit.repository.AuditLogRepository;
import com.frytes.cinemaPlus.booking.event.BookingPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking-events-topic", groupId = "audit-group")
    public void handleBookingPaid(BookingPaidEvent event) {
        log.info("🕵️ [AUDIT] Записываем событие в историю: Order ID {}", event.orderId());

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType("BOOKING_PAID");

            String jsonPayload = objectMapper.writeValueAsString(event);
            auditLog.setPayload(jsonPayload);

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("❌ Ошибка сохранения аудита", e);
            throw new RuntimeException("БД аудита недоступна", e);
        }
    }
}
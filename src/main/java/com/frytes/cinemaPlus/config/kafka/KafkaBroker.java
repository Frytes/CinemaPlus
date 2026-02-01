package com.frytes.cinemaPlus.config.kafka;

import com.frytes.cinemaPlus.common.service.MessageBroker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Profile("!light")
@RequiredArgsConstructor
public class KafkaBroker implements MessageBroker {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public CompletableFuture<Void> send(String topic, String key, Object data) {
        return kafkaTemplate.send(topic, key, data)
                .thenAccept(result -> {
                })
                .thenApply(result -> null);
    }
}
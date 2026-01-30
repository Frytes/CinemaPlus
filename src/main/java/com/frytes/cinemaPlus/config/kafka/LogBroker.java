package com.frytes.cinemaPlus.config.kafka;

import com.frytes.cinemaPlus.common.service.MessageBroker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Profile("light")
public class LogBroker implements MessageBroker {

    @Override
    public CompletableFuture<Void> send(String topic, String key, Object data) {
        log.warn("📝 [LIGHT MODE] Сообщение для Kafka перехвачено: Topic={}, Key={}, Data={}",
                topic, key, data);
        return CompletableFuture.completedFuture(null);
    }
}
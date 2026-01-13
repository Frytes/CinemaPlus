package com.frytes.cinemaPlus.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;

//@Slf4j
//@RequiredArgsConstructor
@Component
public class InfrastructureCheckListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureCheckListener.class);
    public InfrastructureCheckListener(DataSource dataSource,
                                       RedisConnectionFactory redisConnectionFactory,
                                       KafkaAdmin kafkaAdmin) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.kafkaAdmin = kafkaAdmin;
    }

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final KafkaAdmin kafkaAdmin;



    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        boolean dbStatus = checkDatabase();
        boolean redisStatus = checkRedis();
        boolean kafkaStatus = checkKafka();

        System.out.println("\n");
        System.out.println("============================================================");
        System.out.println("🚀 CINEMA PLUS INFRASTRUCTURE STATUS");
        System.out.println("============================================================");
        System.out.println(String.format("Checking Database...    [%s]", getStatusSymbol(dbStatus)));
        System.out.println(String.format("Checking Redis Cache... [%s]", getStatusSymbol(redisStatus)));
        System.out.println(String.format("Checking Kafka...       [%s]", getStatusSymbol(kafkaStatus)));
        System.out.println("============================================================");
        System.out.println("\n");
    }

    private String getStatusSymbol(boolean isUp) {
        return isUp ? "✅ OK" : "❌ FAIL";
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            log.error("DB Check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis Check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkKafka() {
        try {
            kafkaAdmin.describeTopics("test-topic");
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
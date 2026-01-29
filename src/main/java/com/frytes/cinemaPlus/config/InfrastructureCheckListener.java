package com.frytes.cinemaPlus.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class InfrastructureCheckListener implements ApplicationListener<ApplicationReadyEvent> {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final KafkaAdmin kafkaAdmin;
    private final JavaMailSender mailSender;
    private final RestClient restClient;

    public InfrastructureCheckListener(
            DataSource dataSource,
            RedisConnectionFactory redisConnectionFactory,
            KafkaAdmin kafkaAdmin,
            JavaMailSender mailSender,
            RestClient.Builder restClientBuilder
    ) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.kafkaAdmin = kafkaAdmin;
        this.mailSender = mailSender;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        boolean dbStatus = checkDatabase();
        boolean redisStatus = checkRedis();
        boolean kafkaStatus = checkKafka();
        boolean mailStatus = checkMail();
        boolean lokiStatus = checkHttpService("http://localhost:3100/ready", "Loki");
        boolean prometheusStatus = checkHttpService("http://localhost:9090/-/healthy", "Prometheus");

        System.out.println("\n");
        System.out.println("============================================================");
        System.out.println("🚀 CINEMA PLUS INFRASTRUCTURE STATUS");
        System.out.println("============================================================");
        System.out.printf("Checking Database...    [%s]%n", getStatusSymbol(dbStatus));
        System.out.printf("Checking Redis Cache... [%s]%n", getStatusSymbol(redisStatus));
        System.out.printf("Checking Kafka...       [%s]%n", getStatusSymbol(kafkaStatus));
        System.out.printf("Checking Mail Server... [%s]%n", getStatusSymbol(mailStatus));
        System.out.printf("Checking Loki Log...    [%s]%n", getStatusSymbol(lokiStatus));
        System.out.printf("Checking Prometheus...  [%s]%n", getStatusSymbol(prometheusStatus));
        System.out.println("============================================================");
        System.out.println("\n");

    }

    private String getStatusSymbol(boolean isUp) {
        return isUp ? "\u001B[32m✅ OK\u001B[0m" : "\u001B[31m❌ FAIL\u001B[0m";
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
        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            client.describeCluster().clusterId().get(3, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.warn("⚠️ Kafka check failed (it might still be starting up): {}", e.getMessage());
            return false;
        }
    }

    private boolean checkMail() {
        try {
            if (mailSender instanceof JavaMailSenderImpl mailImpl) {
                mailImpl.testConnection();
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Mail Check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkHttpService(String url, String serviceName) {
        try {
            restClient.get()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("⚠️ {} check failed: {}", serviceName, e.getMessage());
            return false;
        }
    }
}
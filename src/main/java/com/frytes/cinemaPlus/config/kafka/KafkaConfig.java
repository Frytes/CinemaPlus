package com.frytes.cinemaPlus.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Profile("!light")
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("booking-events-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
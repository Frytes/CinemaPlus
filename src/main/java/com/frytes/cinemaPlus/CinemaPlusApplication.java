package com.frytes.cinemaPlus;

import com.frytes.cinemaPlus.simulation.config.StressProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties(StressProperties.class)
public class CinemaPlusApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemaPlusApplication.class, args);

	}
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
	}
}

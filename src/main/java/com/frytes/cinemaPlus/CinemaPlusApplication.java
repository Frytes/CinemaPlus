package com.frytes.cinemaPlus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CinemaPlusApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemaPlusApplication.class, args);
	}

}

package com.frytes.cinemaPlus;

import org.springframework.boot.SpringApplication;

public class TestCinemaPlusApplication {

	public static void main(String[] args) {
		SpringApplication.from(CinemaPlusApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

package com.hexaware.cozy_heaven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CozyHeavenBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CozyHeavenBackendApplication.class, args);
	}

}

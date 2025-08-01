package com.onion.emailnotificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmailnotificationserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailnotificationserviceApplication.class, args);
	}

}

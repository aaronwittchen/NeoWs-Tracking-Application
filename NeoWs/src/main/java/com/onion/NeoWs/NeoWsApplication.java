package com.onion.NeoWs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // enables scheduling support in the application, allowing us to use @Scheduled annotations for periodic tasks
public class NeoWsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoWsApplication.class, args);
	}

}

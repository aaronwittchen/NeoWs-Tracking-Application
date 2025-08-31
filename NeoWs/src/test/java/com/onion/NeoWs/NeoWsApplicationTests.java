package com.onion.NeoWs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.onion.NeoWs.client.NasaClient;
import com.onion.NeoWs.event.AsteroidCollisionEvent;

@SpringBootTest
@ActiveProfiles("test")
class NeoWsApplicationTests {

	@MockBean
	private KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate;

	@MockBean
	private NasaClient nasaClient;

	@MockBean
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

}

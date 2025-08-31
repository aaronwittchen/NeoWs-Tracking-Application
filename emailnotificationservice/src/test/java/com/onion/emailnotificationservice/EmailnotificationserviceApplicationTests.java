package com.onion.emailnotificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.onion.NeoWs.event.AsteroidCollisionEvent;
import com.onion.emailnotificationservice.repository.NotificationRepository;
import com.onion.emailnotificationservice.repository.UserRepository;
import com.onion.emailnotificationservice.service.EmailContentBuilder;
import com.onion.emailnotificationservice.service.EmailSenderService;
import com.onion.emailnotificationservice.service.EmailService;
import com.onion.emailnotificationservice.service.NasaApodService;
import com.onion.emailnotificationservice.service.NotificationProcessor;

@SpringBootTest
@ActiveProfiles("test")
class EmailnotificationserviceApplicationTests {

	@MockBean
	private KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate;

	@MockBean
	private EmailService emailService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private NotificationProcessor notificationProcessor;

	@MockBean
	private EmailContentBuilder emailContentBuilder;

	@MockBean
	private EmailSenderService emailSenderService;

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private NasaApodService nasaApodService;

	@MockBean
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

}

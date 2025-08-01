package com.onion.NeoWs.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.onion.NeoWs.client.NasaClient;
import com.onion.NeoWs.dto.Asteroid;
import com.onion.NeoWs.event.AsteroidCollisionEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j // Lombok annotation to generate a logger instance
public class AlertService {

    private final NasaClient nasaClient;
    private final KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate;

    // @Autowired
    public AlertService(NasaClient nasaClient, KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate) {
        this.nasaClient = nasaClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void alert() {
        log.info("Alerting service triggered");

        final LocalDate fromDate = LocalDate.now();
        final LocalDate toDate = LocalDate.now().plusDays(7);

        // Call NASA API to get the asteroid data
        log.info("Getting asteroid data from {} to {}", fromDate, toDate);
        final List<Asteroid> asteroidList;
        try {
            asteroidList = nasaClient.getNeoAsteroids(fromDate, toDate);
        } catch (Exception e) {
            log.error("Failed to fetch asteroid data from NASA API", e);
            throw new RuntimeException("Failed to fetch asteroid data from NASA API: " + e.getMessage(), e);
        }
        log.info("Received {} asteroids from NASA API", asteroidList.size());

        // If there are any hazardous asteroids, send an email alert
        final List<Asteroid> hazardousAsteroids = asteroidList.stream()
                .filter(Asteroid::getIsPotentiallyHazardousAsteroid)
                .toList();
        log.info("Found {} hazardous asteroids", hazardousAsteroids.size());

        // Create an alert and put on Kafka topic
        final List<AsteroidCollisionEvent> asteroidCollisionEventList = createEventListOfHazardousAsteroids(hazardousAsteroids);

        log.info("Sending {} asteroid collision events to Kafka topic", asteroidCollisionEventList.size());
        asteroidCollisionEventList.forEach(event -> {
            try {
                kafkaTemplate.send("asteroid-alert", event).get();
                log.info("Sent asteroid collision event for asteroid: {}", event);
            } catch (Exception e) {
                log.error("Failed to send asteroid collision event to Kafka for asteroid: {}", event, e);
                throw new RuntimeException("Failed to send asteroid collision event to Kafka: " + e.getMessage(), e);
            }
        });
    }


    private List<AsteroidCollisionEvent> createEventListOfHazardousAsteroids(final List<Asteroid> dangerousAsteroids) {
        return dangerousAsteroids.stream()
                .map(this::createAsteroidCollisionEvent)
                .toList();
    }
    
    private AsteroidCollisionEvent createAsteroidCollisionEvent(Asteroid asteroid) {
        return AsteroidCollisionEvent.builder()
                .asteroidName(asteroid.getName())
                .closeApproachDate(asteroid.getCloseApproachData().getFirst().getCloseApproachDate().toString())
                .missDistanceKilometers(asteroid.getCloseApproachData().getFirst().getMissDistance().getKilometers())
                .estimatedDiameterAverageMeters((asteroid.getEstimatedDiameter().getMeters().getMinDiameter() +
                        asteroid.getEstimatedDiameter().getMeters().getMaxDiameter()) / 2)
                .build();
    }
}
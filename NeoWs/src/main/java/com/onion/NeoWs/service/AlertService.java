/**
 * The AlertService class is a Spring service responsible for orchestrating the asteroid alert workflow.
 * It interacts with NASA's NeoWs (Near-Earth Object Web Service) API to fetch asteroid data, filters
 * for potentially hazardous asteroids, and publishes collision events to a Kafka topic for further
 * processing (e.g., sending email notifications). This service is a core component of the asteroid
 * monitoring system, handling data retrieval, filtering, and event publishing.
 * 
 * Key Features:
 * - Fetches asteroid data for a specified date range (default: current date to 7 days in the future).
 * - Filters asteroids to identify those marked as potentially hazardous.
 * - Publishes collision events for hazardous asteroids to a Kafka topic asynchronously.
 * - Handles errors gracefully with custom exceptions and logging.
 * 
 * Dependencies:
 * - NasaClient: For interacting with NASA's NeoWs API.
 * - KafkaTemplate: For publishing events to a Kafka topic.
 * - Lombok: For logging and reducing boilerplate code.
 * - CompletableFuture: For asynchronous event publishing.
 */
package com.onion.NeoWs.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.onion.NeoWs.client.NasaClient;
import com.onion.NeoWs.dto.Asteroid;
import com.onion.NeoWs.event.AsteroidCollisionEvent;
import com.onion.NeoWs.exception.NasaApiException;
import com.onion.NeoWs.exception.KafkaPublishingException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlertService {

    private final NasaClient nasaClient;
    private final KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate;

    /**
     * Constructor-based dependency injection for NasaClient and KafkaTemplate.
     * 
     * @param nasaClient The client for fetching asteroid data from NASA's NeoWs API.
     * @param kafkaTemplate The Kafka template for publishing asteroid collision events.
     */
    public AlertService(NasaClient nasaClient, KafkaTemplate<String, AsteroidCollisionEvent> kafkaTemplate) {
        this.nasaClient = nasaClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Triggers the asteroid alert workflow:
     * 1. Fetches asteroid data for the next 7 days from NASA's NeoWs API.
     * 2. Filters for potentially hazardous asteroids.
     * 3. Publishes collision events for hazardous asteroids to a Kafka topic.
     * 
     * Logs the progress and handles errors by throwing custom exceptions.
     */
    public void alert() {
        log.info("Alerting service triggered");

        final LocalDate fromDate = LocalDate.now();
        final LocalDate toDate = LocalDate.now().plusDays(7);

        // Fetch asteroid data
        final List<Asteroid> asteroidList = fetchAsteroidData(fromDate, toDate);
        log.info("Received {} asteroids from NASA API", asteroidList.size());

        // Filter hazardous asteroids
        final List<Asteroid> hazardousAsteroids = asteroidList.stream()
                .filter(asteroid -> asteroid.getIsPotentiallyHazardousAsteroid() != null && 
                                   asteroid.getIsPotentiallyHazardousAsteroid())
                .toList();
        log.info("Found {} hazardous asteroids", hazardousAsteroids.size());

        if (hazardousAsteroids.isEmpty()) {
            log.info("No hazardous asteroids found, no alerts to send");
            return;
        }

        // Create and send events
        publishAsteroidEvents(hazardousAsteroids);
        log.info("Alert processing completed successfully");
    }

    /**
     * Fetches asteroid data from NASA's NeoWs API for the specified date range.
     * 
     * @param fromDate The start date for fetching asteroid data.
     * @param toDate The end date for fetching asteroid data.
     * @return A list of Asteroid objects.
     * @throws NasaApiException If the API call fails.
     */
    private List<Asteroid> fetchAsteroidData(LocalDate fromDate, LocalDate toDate) {
        log.info("Getting asteroid data from {} to {}", fromDate, toDate);
        try {
            return nasaClient.getNeoAsteroids(fromDate, toDate);
        } catch (Exception e) {
            log.error("Failed to fetch asteroid data from NASA API", e);
            throw new NasaApiException("Failed to fetch asteroid data from NASA API: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes collision events for hazardous asteroids to a Kafka topic asynchronously.
     * Waits for all events to be sent before completing.
     * 
     * @param hazardousAsteroids The list of hazardous asteroids to process.
     * @throws KafkaPublishingException If event publishing fails.
     */
    private void publishAsteroidEvents(List<Asteroid> hazardousAsteroids) {
        final List<AsteroidCollisionEvent> events = createEventListOfHazardousAsteroids(hazardousAsteroids);
        log.info("Sending {} asteroid collision events to Kafka topic", events.size());

        // Send all asynchronously and wait for all to complete
        List<CompletableFuture<Void>> futures = events.stream()
                .map(this::sendEventAsync)
                .toList();

        // Wait for all to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get(); // This will throw if any failed
            log.info("Successfully sent all {} events to Kafka", events.size());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send some events to Kafka", e);
            throw new KafkaPublishingException("Failed to send events to Kafka: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a single asteroid collision event to the Kafka topic asynchronously.
     * 
     * @param event The AsteroidCollisionEvent to send.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    private CompletableFuture<Void> sendEventAsync(AsteroidCollisionEvent event) {
        return kafkaTemplate.send("asteroid-alert", event)
                .thenAccept(result -> log.debug("Sent asteroid collision event for: {}", event.getAsteroidName()))
                .exceptionally(throwable -> {
                    log.error("Failed to send event for asteroid: {}", event.getAsteroidName(), throwable);
                    throw new RuntimeException("Failed to send event", throwable);
                });
    }

    /**
     * Converts a list of hazardous asteroids into a list of AsteroidCollisionEvent objects.
     * 
     * @param dangerousAsteroids The list of hazardous asteroids.
     * @return A list of AsteroidCollisionEvent objects, excluding any failed conversions.
     */
    private List<AsteroidCollisionEvent> createEventListOfHazardousAsteroids(final List<Asteroid> dangerousAsteroids) {
        return dangerousAsteroids.stream()
                .map(this::createAsteroidCollisionEvent)
                .filter(event -> event != null) // Filter out any failed conversions
                .toList();
    }
    
    /**
     * Creates an AsteroidCollisionEvent from an Asteroid object.
     * Performs null checks and logs warnings for invalid data.
     * 
     * @param asteroid The Asteroid object to convert.
     * @return An AsteroidCollisionEvent, or null if the conversion fails.
     */
    private AsteroidCollisionEvent createAsteroidCollisionEvent(Asteroid asteroid) {
        try {
            // Null safety checks
            if (asteroid.getCloseApproachData() == null || asteroid.getCloseApproachData().isEmpty()) {
                log.warn("No close approach data available for asteroid: {}", asteroid.getName());
                return null;
            }

            if (asteroid.getEstimatedDiameter() == null || 
                asteroid.getEstimatedDiameter().getMeters() == null) {
                log.warn("No diameter data available for asteroid: {}", asteroid.getName());
                return null;
            }

            var closeApproach = asteroid.getCloseApproachData().getFirst();
            var meters = asteroid.getEstimatedDiameter().getMeters();

            return AsteroidCollisionEvent.builder()
                    .asteroidName(asteroid.getName())
                    .closeApproachDate(closeApproach.getCloseApproachDate().toString())
                    .missDistanceKilometers(closeApproach.getMissDistance().getKilometers())
                    .estimatedDiameterAverageMeters((meters.getMinDiameter() + meters.getMaxDiameter()) / 2)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create event for asteroid: {}", asteroid.getName(), e);
            return null; // Skip this asteroid rather than failing the entire process
        }
    }
}
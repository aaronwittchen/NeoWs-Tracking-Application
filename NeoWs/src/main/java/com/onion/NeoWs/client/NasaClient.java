/**
 * The NasaClient class is a Spring service responsible for interacting with NASA's NeoWs (Near-Earth Object Web Service) API.
 * It retrieves data about Near-Earth Objects (NEOs) for a specified date range, validates the input, and constructs API requests.
 * The service handles HTTP communication using RestTemplate and processes the API response into a list of Asteroid objects.
 * 
 * Key Features:
 * - Fetches asteroid data from NASA's NeoWs API for a given date range.
 * - Validates date ranges to ensure compliance with NASA's API restrictions (e.g., maximum 7-day range).
 * - Handles errors gracefully with logging and custom exceptions.
 * - Uses configuration properties for the API URL and key to avoid hardcoding sensitive data.
 * 
 * Dependencies:
 * - Spring's RestTemplate: For making HTTP requests to the NASA API.
 * - Lombok: For logging and reducing boilerplate code.
 * - NasaNeoResponse and Asteroid DTOs: For deserializing and processing API responses.
 */
package com.onion.NeoWs.client;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.onion.NeoWs.dto.Asteroid;
import com.onion.NeoWs.dto.NasaNeoResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NasaClient {

    private final RestTemplate restTemplate;
    
    @Value("${nasa.neo.api.url}")
    private String neoApiUrl;

    @Value("${nasa.api.key}")
    private String apiKey;

    /**
     * Constructor-based dependency injection for RestTemplate.
     * 
     * @param restTemplate The RestTemplate bean for making HTTP requests.
     */
    // Inject RestTemplate as a bean instead of creating new instances
    public NasaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves a list of asteroids from NASA's NeoWs API for the specified date range.
     * Validates the date range, constructs the API URL, and processes the response.
     * 
     * @param fromDate The start date for fetching asteroid data.
     * @param toDate The end date for fetching asteroid data.
     * @return A list of Asteroid objects, or an empty list if no data is available.
     * @throws RuntimeException If the API call or response processing fails.
     */
    public List<Asteroid> getNeoAsteroids(final LocalDate fromDate, final LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        
        final String url = getUrl(fromDate, toDate);
        log.info("Calling NASA NEO API: {}", url.replaceAll("api_key=[^&]*", "api_key=***")); // Hide API key in logs
        
        try {
            final NasaNeoResponse nasaNeoResponse = restTemplate.getForObject(url, NasaNeoResponse.class);
            
            if (nasaNeoResponse == null) {
                log.warn("Received null response from NASA API");
                return new ArrayList<>();
            }
            
            if (nasaNeoResponse.getNearEarthObjects() == null) {
                log.warn("No near earth objects data in NASA API response");
                return new ArrayList<>();
            }
            
            // More efficient - do everything in one stream operation
            List<Asteroid> asteroidList = nasaNeoResponse
                    .getNearEarthObjects()
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .toList();
                    
            log.info("Successfully retrieved {} asteroids from NASA API", asteroidList.size());
            return asteroidList;
            
        } catch (RestClientException e) {
            log.error("Failed to call NASA API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch asteroid data from NASA API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while processing NASA API response: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error processing asteroid data: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the date range to ensure it meets NASA's API requirements.
     * 
     * @param fromDate The start date.
     * @param toDate The end date.
     * @throws IllegalArgumentException If the date range is invalid (null, reversed, or exceeds 7 days).
     */
    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("From date and to date cannot be null");
        }
        
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        
        // NASA API has a 7-day limit for date ranges
        long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
        if (daysBetween > 7) {
            throw new IllegalArgumentException("Date range cannot exceed 7 days (NASA API limitation)");
        }
    }

    /**
     * Constructs the NASA API URL with the provided date range and API key.
     * 
     * @param fromDate The start date for the query.
     * @param toDate The end date for the query.
     * @return The constructed API URL as a string.
     */
    private String getUrl(final LocalDate fromDate, final LocalDate toDate) {
        return UriComponentsBuilder.fromUriString(neoApiUrl)
                .queryParam("start_date", fromDate.toString())
                .queryParam("end_date", toDate.toString())
                .queryParam("api_key", apiKey)
                .toUriString();
    }
}
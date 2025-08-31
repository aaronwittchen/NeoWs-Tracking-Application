package com.onion.emailnotificationservice.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NasaApodService {

    @Value("${nasa.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private static final String APOD_API_URL = "https://api.nasa.gov/planetary/apod";

    public NasaApodService() {
        this.restTemplate = new RestTemplate();
    }

    public ApodResponse getApodForDate(LocalDate date) {
        try {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String url = String.format("%s?date=%s&api_key=%s", APOD_API_URL, dateStr, apiKey);
            
            log.info("Fetching APOD for date: {}", dateStr);
            
            ResponseEntity<ApodResponse> response = restTemplate.getForEntity(url, ApodResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ApodResponse apodResponse = response.getBody();
                
                // Check if the API returned an error
                if (apodResponse.getError() != null) {
                    log.warn("APOD API returned error: {} (Code: {}) for date: {}", 
                            apodResponse.getError(), apodResponse.getCode(), dateStr);
                    return null;
                }
                
                log.info("Successfully fetched APOD: {} (Date: {})", apodResponse.getTitle(), apodResponse.getDate());
                return apodResponse;
            } else {
                log.warn("APOD API returned non-OK status: {} for date: {}", response.getStatusCode(), dateStr);
                return null;
            }
            
        } catch (ResourceAccessException e) {
            log.error("Network error fetching APOD for date {}: {}", date, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch APOD for date {}: {}", date, e.getMessage(), e);
            return null;
        }
    }

    public ApodResponse getApodForToday() {
        return getApodForDate(LocalDate.now());
    }

    @Data
    public static class ApodResponse {
        @JsonProperty("copyright")
        private String copyright;
        
        @JsonProperty("date")
        private String date;
        
        @JsonProperty("explanation")
        private String explanation;
        
        @JsonProperty("hdurl")
        private String hdurl;
        
        @JsonProperty("media_type")
        private String mediaType;
        
        @JsonProperty("service_version")
        private String serviceVersion;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("error")
        private String error;
        
        @JsonProperty("code")
        private Integer code;
    }
} 
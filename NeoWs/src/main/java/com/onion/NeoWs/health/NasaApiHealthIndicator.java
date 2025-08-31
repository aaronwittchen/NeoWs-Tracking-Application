package com.onion.NeoWs.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NasaApiHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String nasaApiKey;

    public NasaApiHealthIndicator(@Value("${nasa.api.key}") String nasaApiKey) {
        this.nasaApiKey = nasaApiKey;
    }

    @Override
    public Health health() {
        String url = "https://api.nasa.gov/neo/rest/v1/feed?api_key=" + nasaApiKey;
        try {
            restTemplate.getForObject(url, String.class);
            return Health.up().withDetail("NASA API", "Available").build();
        } catch (Exception e) {
            return Health.down(e).withDetail("NASA API", "Unavailable").build();
        }
    }
}

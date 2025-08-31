package com.onion.NeoWs.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the NASA Near-Earth Object Web Service (NeoWs) API response.
 * 
 * The NASA API returns asteroid data grouped by date, where each date maps to a list of asteroids
 * that have close approaches on that date. This DTO captures the essential structure needed
 * for processing potentially hazardous asteroids.
 * 
 * Key fields extracted from the NASA API response:
 * - near_earth_objects: Map of date strings to lists of asteroids
 * - element_count: Total number of asteroids in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NasaNeoResponse {
    
    /**
     * Map where keys are date strings (YYYY-MM-DD) and values are lists of asteroids
     * that have close approaches on that date.
     */
    @JsonProperty("near_earth_objects")
    private Map<String, List<Asteroid>> nearEarthObjects;

    /**
     * Total count of asteroids included in this response.
     */
    @JsonProperty("element_count")
    private Long elementCount;
    
    /**
     * API request information and links (optional field from NASA response).
     */
    @JsonProperty("links")
    private Object links;
}
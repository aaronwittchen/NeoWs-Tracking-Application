package com.onion.NeoWs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an asteroid from the NASA Near-Earth Object Web Service.
 * 
 * This class maps the essential asteroid properties from the NASA API response,
 * focusing on identification, size estimation, hazard assessment, and close approach data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unmapped fields from NASA API
public class Asteroid {

    /**
     * Unique identifier for the asteroid from NASA's database.
     */
    private String id;

    /**
     * Common name or designation of the asteroid.
     */
    private String name;

    /**
     * Estimated diameter information in various units.
     * Contains nested object with diameter ranges in meters, kilometers, etc.
     */
    @JsonProperty("estimated_diameter")
    private EstimatedDiameter estimatedDiameter;

    /**
     * NASA's assessment of whether this asteroid poses a potential hazard to Earth.
     * Based on size and minimum orbit intersection distance (MOID).
     */
    @JsonProperty("is_potentially_hazardous_asteroid")
    private Boolean isPotentiallyHazardousAsteroid;

    /**
     * List of close approach events for this asteroid.
     * Each entry contains details about when the asteroid approaches Earth,
     * including date, distance, and velocity information.
     */
    @JsonProperty("close_approach_data")
    private List<CloseApproachData> closeApproachData;
}
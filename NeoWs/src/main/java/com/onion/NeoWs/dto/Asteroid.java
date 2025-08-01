package com.onion.NeoWs.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // This annotation tells Jackson to ignore any unknown properties in the JSON response that are not mapped to the class fields, preventing deserialization errors.
public class Asteroid {

    private String id;
    private String name;

    /**
     * we can see that this is not a single
entry this is actually an object within the asteroid object so you need to
Define it as an object inside your asteroid class
     */
    @JsonProperty("estimated_diameter")
    private EstimatedDiameter estimatedDiameter;

    @JsonProperty("is_potentially_hazardous_asteroid")
    private Boolean isPotentiallyHazardousAsteroid;

    @JsonProperty("close_approach_data")
    private List<CloseApproachData> closeApproachData;
    
}

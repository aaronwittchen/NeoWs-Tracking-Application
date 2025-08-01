package com.onion.NeoWs.dto;

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
public class DiameterRange {
    
    @JsonProperty("estimated_diameter_min")
    private double  minDiameter;

    @JsonProperty("estimated_diameter_max")
    private double maxDiameter;
}

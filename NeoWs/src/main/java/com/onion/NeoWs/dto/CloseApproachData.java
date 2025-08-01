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
@JsonIgnoreProperties(ignoreUnknown = true) 
public class CloseApproachData {

    @JsonProperty("close_approach_date")
    private String closeApproachDate;

    @JsonProperty("miss_distance")
    private MissDistance missDistance;
    
}

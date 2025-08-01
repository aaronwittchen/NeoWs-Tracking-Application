package com.onion.NeoWs.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * create these objects over here so these are some data transfer objects also
called dtos I'll create a dto package what we want to do now is recreate this
structure in Spring boot so we can see that this is a map and now we need to determine the other properties that we
think are important and we're going to ignore the rest so for example we'll be getting the ID the name we will be
getting the diameter in meters I guess important as well this flag here and we
want the close approach date and the Mis distance as well I think that's quite
interesting to be sent in a notification
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NasaNeoResponse {
    
    @JsonProperty("near_earth_objects") // this is the name of the property in the JSON response
    private Map<String, List<Asteroid>> nearEarthObjects; // this is a map where the key is a date string and the value is a list of Asteroid objects

    @JsonProperty("element_count")
    private Long totalAsteroids;
}

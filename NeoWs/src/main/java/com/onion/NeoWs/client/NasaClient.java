package com.onion.NeoWs.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.onion.NeoWs.dto.Asteroid;
import com.onion.NeoWs.dto.NasaNeoResponse;

/**
 * This class is responsible for making calls to the NASA API to fetch Near-Earth Object (NEO) data.
 * It uses Spring's RestTemplate to perform HTTP requests.
 */

@Service
public class NasaClient {

    @Value("${nasa.neo.api.url}")
    private String neoApiUrl;

    @Value("${nasa.api.key}")
    private String apiKey;

    public List<Asteroid> getNeoAsteroids(final LocalDate fromDate, final LocalDate toDate) {
        final RestTemplate restTemplate = new RestTemplate(); // create a new RestTemplate instance that makes endpoint calls to the NASA API

        final NasaNeoResponse nasaNeoResponse = restTemplate.getForObject(getUrl(fromDate, toDate), NasaNeoResponse.class); // using the RestTemplate to get a Masa Neo Response object from the NASA API
    
        List<Asteroid> asteroidList = new ArrayList<>();
        if (nasaNeoResponse != null) {
            // The response contains a map of dates to lists of asteroids, so we need to flatten this structure
            asteroidList.addAll(nasaNeoResponse
                .getNearEarthObjects() // get the map of near-Earth objects
                .values() // get the collection of lists of asteroids
                .stream() // stream the values of the map
                .flatMap(List::stream) // flatten the lists into a single stream of Asteroid objects
                .toList()); // collect the stream into a list
        }

        return asteroidList;
    }

    /**
     * Constructs the URL for the NASA API call to fetch Near-Earth Object data within a specified date range.
     * This method uses the UriComponentsBuilder to build the URL with query parameters for start and end dates, and the API key.
     * @param fromDate
     * @param toDate
     * @return
     */
    public String getUrl (final LocalDate fromDate, final LocalDate toDate) {
        String apiUrl = UriComponentsBuilder.fromUriString(neoApiUrl)
                .queryParam("start_date", fromDate)
                .queryParam("end_date", toDate)
                .queryParam("api_key", apiKey)
                .toUriString();
        return apiUrl;
    }
}

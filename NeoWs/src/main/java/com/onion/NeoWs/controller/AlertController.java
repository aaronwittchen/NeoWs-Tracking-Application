package com.onion.NeoWs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.onion.NeoWs.service.AlertService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/asteroid-alerts")
@Tag(name = "Asteroid Alerts", description = "API endpoints for triggering asteroid alerts and monitoring Near-Earth Objects")
public class AlertController {

    private final AlertService alertService;

    // @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }
    
    @GetMapping("/health")
    @Operation(
        summary = "Health Check",
        description = "Check if the asteroid alert service is running"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy"),
        @ApiResponse(responseCode = "500", description = "Service is not healthy")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "NeoWs Asteroid Alert Service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/alert") // endpoint that we're going to call to trigger the whole flow
    @ResponseStatus(HttpStatus.ACCEPTED) // indicates that the request has been accepted for processing, but the processing is not complete
    @Operation(
        summary = "Trigger Asteroid Alert",
        description = "Triggers the asteroid alert system to fetch data from NASA API, identify hazardous asteroids, and send email alerts"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Alert request accepted and processing started"),
        @ApiResponse(responseCode = "500", description = "Internal server error during alert processing")
    })
    public void triggerAlert() {
        alertService.alert();
    }
}

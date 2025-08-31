/**
 * The AlertController class is a REST controller that provides API endpoints for managing asteroid alerts
 * and monitoring Near-Earth Objects (NEOs) using NASA's NeoWs (Near-Earth Object Web Service) API.
 * It handles health checks for the service and triggers the alert processing workflow, which involves
 * fetching asteroid data, identifying hazardous asteroids, and sending email notifications.
 * 
 * This controller is part of a larger system designed to monitor and alert users about potentially
 * hazardous asteroids by interacting with the AlertService.
 * 
 * Key Features:
 * - Health check endpoint to verify service status.
 * - Trigger endpoint to initiate the asteroid alert workflow.
 * 
 * Dependencies:
 * - Spring Boot for REST API functionality.
 * - Swagger (OpenAPI) for API documentation.
 * - Lombok for logging and reducing boilerplate code.
 */
package com.onion.NeoWs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onion.NeoWs.service.AlertService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/asteroid-alerts")
@Tag(name = "Asteroid Alerts", description = "API endpoints for triggering asteroid alerts and monitoring Near-Earth Objects")
public class AlertController {

    private final AlertService alertService;

    /**
     * Constructor-based dependency injection for AlertService.
     * 
     * @param alertService The service responsible for handling the asteroid alert logic.
     */
    // @Autowired (Commented out as constructor injection is preferred for better testability)
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }
    
    /**
     * Health check endpoint to verify if the asteroid alert service is operational.
     * Returns a JSON response with service status, name, timestamp, and version.
     * 
     * @return ResponseEntity containing a map with health check details.
     */
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
    
    /**
     * Triggers the asteroid alert workflow by calling the AlertService.
     * The workflow fetches data from NASA's NeoWs API, identifies hazardous asteroids,
     * and sends email alerts. Returns a JSON response indicating success or failure.
     * 
     * @return ResponseEntity containing a map with the result of the alert processing.
     */
    @PostMapping("/alert")
    @Operation(
        summary = "Trigger Asteroid Alert",
        description = "Triggers the asteroid alert system to fetch data from NASA API, identify hazardous asteroids, and send email alerts"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alert processing completed successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error during alert processing")
    })
    public ResponseEntity<Map<String, Object>> triggerAlert() {
        try {
            // This will block until processing is complete
            alertService.alert();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Alert processing completed");
            response.put("timestamp", LocalDateTime.now());
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Alert processing failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Alert processing failed");
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body(errorResponse);
        }
    }
}
package com.onion.emailnotificationservice.controller;

import com.onion.emailnotificationservice.dto.UserDto;
import com.onion.emailnotificationservice.dto.UserRegistrationRequest;
import com.onion.emailnotificationservice.dto.NotificationToggleRequest;
import com.onion.emailnotificationservice.service.UserService;
import com.onion.emailnotificationservice.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users and notification preferences")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with default notifications disabled"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User with email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received user registration request for email: {}", request.getEmail());
        
        try {
            UserDto createdUser = userService.createUser(request);
            log.info("User registered successfully: {}", createdUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            log.warn("User registration failed: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all registered users"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Fetching all users");
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/notifications-enabled")
    @Operation(
        summary = "Get users with notifications enabled",
        description = "Retrieves a list of users who have notifications enabled"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<UserDto>> getUsersWithNotificationsEnabled() {
        log.info("Fetching users with notifications enabled");
        List<UserDto> users = userService.getUsersWithNotificationsEnabled();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/notification")
    @Operation(
        summary = "Update notification preference",
        description = "Updates a user's notification preference (enabled/disabled)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification preference updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> updateNotificationPreference(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody NotificationToggleRequest request) {
        log.info("Updating notification preference for user ID: {} to: {}", id, request.isNotificationEnabled());
        
        try {
            UserDto updatedUser = userService.updateNotificationPreference(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update notification preference: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/send-alerts")
    @Operation(
        summary = "Send NASA Asteroid Alert emails",
        description = "Triggers sending NASA Asteroid Alert emails to all users with notifications enabled."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Email sending triggered successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> sendAsteroidAlerts() {
        log.info("Triggering NASA Asteroid Alert emails to all notification-enabled users");
        emailService.sendAsteroidAlertEmail();
        return ResponseEntity.accepted().body("NASA Asteroid Alert emails are being sent to all notification-enabled users.");
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Deletes a user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete user: {}", e.getMessage());
            throw e;
        }
    }
} 
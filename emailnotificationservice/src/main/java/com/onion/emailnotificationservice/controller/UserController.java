package com.onion.emailnotificationservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onion.emailnotificationservice.entity.User;
import com.onion.emailnotificationservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRegistrationRequest request) {
        try {
            log.info("Received user registration request for email: {}", request.getEmail());

            // Validate request
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Full name is required"));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Email is required"));
            }

            // Check if user already exists
            List<User> existingUsers = userRepository.findByEmail(request.getEmail());
            if (!existingUsers.isEmpty()) {
                log.warn("User registration failed - email already exists: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("A user with this email already exists"));
            }

            // Create new user
            User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .notificationEnabled(request.isNotificationEnabled())
                .build();

            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", savedUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(savedUser.getId(), savedUser.getFullName(), 
                                     savedUser.getEmail(), savedUser.isNotificationEnabled()));

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An error occurred while creating the user"));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request/Response DTOs
    public static class UserRegistrationRequest {
        private String fullName;
        private String email;
        private boolean notificationEnabled = true;

        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public boolean isNotificationEnabled() { return notificationEnabled; }
        public void setNotificationEnabled(boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    }

    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private boolean notificationEnabled;

        public UserResponse(Long id, String fullName, String email, boolean notificationEnabled) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.notificationEnabled = notificationEnabled;
        }

        // Getters
        public Long getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public boolean isNotificationEnabled() { return notificationEnabled; }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
} 
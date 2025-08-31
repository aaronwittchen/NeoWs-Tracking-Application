package com.onion.emailnotificationservice.service;

import com.onion.emailnotificationservice.dto.UserDto;
import com.onion.emailnotificationservice.dto.UserRegistrationRequest;
import com.onion.emailnotificationservice.dto.NotificationToggleRequest;
import com.onion.emailnotificationservice.entity.User;
import com.onion.emailnotificationservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;

    @Transactional
    public UserDto createUser(UserRegistrationRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).size() > 0) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        // Create new user with notifications enabled by default
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .notificationEnabled(true) // Always enabled by default
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // Send welcome email asynchronously (does not block response)
        try {
            emailService.sendWelcomeEmailAsync(savedUser.getEmail(), savedUser.getFullName());
        } catch (Exception e) {
            log.warn("Failed to trigger welcome email for user {}: {}", savedUser.getEmail(), e.getMessage());
        }
        
        return mapToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithNotificationsEnabled() {
        log.info("Fetching users with notifications enabled");
        return userRepository.findAll().stream()
                .filter(User::isNotificationEnabled)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(this::mapToDto);
    }

    @Transactional
    public UserDto updateNotificationPreference(Long userId, NotificationToggleRequest request) {
        log.info("Updating notification preference for user ID: {} to: {}", userId, request.isNotificationEnabled());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setNotificationEnabled(request.isNotificationEnabled());
        User savedUser = userRepository.save(user);
        
        log.info("Notification preference updated successfully for user ID: {}", userId);
        return mapToDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        userRepository.deleteById(userId);
        log.info("User deleted successfully with ID: {}", userId);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .notificationEnabled(user.isNotificationEnabled())
                .build();
    }
}

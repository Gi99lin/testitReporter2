package com.testit.reports.controller;

import com.testit.reports.controller.dto.ApiResponse;
import com.testit.reports.controller.dto.UserDto;
import com.testit.reports.model.entity.User;
import com.testit.reports.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get current user
     *
     * @param userDetails Current user details
     * @return Current user
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting current user. UserDetails: {}", userDetails);
        try {
            if (userDetails == null) {
                log.error("UserDetails is null");
                return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
            }
            
            log.info("Username: {}", userDetails.getUsername());
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            log.info("User found: {}", user);
            return ResponseEntity.ok(ApiResponse.success(UserDto.fromEntity(user)));
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update current user
     *
     * @param userDetails Current user details
     * @param userDto     User data to update
     * @return Updated user
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Prevent role change
            userDto.setRole(user.getRole());
            userDto.setId(user.getId());

            User updatedUser = userService.updateUser(user.getId(), userDto.toEntity());
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", UserDto.fromEntity(updatedUser)));
        } catch (Exception e) {
            log.error("Error updating current user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update current user's TestIT token
     *
     * @param userDetails Current user details
     * @param token       TestIT token
     * @return Updated user
     */
    @PutMapping("/me/token")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrentUserToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody String token) {
        try {
            // Remove quotes if present (happens when sending raw string in request body)
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            
            log.info("Updating TestIT token for user: {}", userDetails.getUsername());
            
            // Get the current authentication token from the request
            String currentAuthToken = null;
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                currentAuthToken = authentication.getCredentials().toString();
                log.info("Current authentication token preserved");
            }
            
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            User updatedUser = userService.updateUserToken(user.getId(), token);
            
            // Restore the original authentication if it was present
            if (currentAuthToken != null) {
                try {
                    // Create a new authentication with the original token
                    org.springframework.security.core.Authentication newAuth = 
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            authentication.getPrincipal(), 
                            currentAuthToken,
                            authentication.getAuthorities());
                    
                    // Set the new authentication in the security context
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);
                    log.info("Original authentication token restored");
                } catch (Exception e) {
                    log.error("Error restoring original authentication", e);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("TestIT token updated successfully", UserDto.fromEntity(updatedUser)));
        } catch (Exception e) {
            log.error("Error updating current user TestIT token", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all users (admin only)
     *
     * @return List of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        try {
            List<UserDto> users = userService.getAllUsers().stream()
                    .map(UserDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user by ID (admin only)
     *
     * @param id User ID
     * @return User
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(UserDto.fromEntity(user)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting user by ID", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create user (admin only)
     *
     * @param userDto User data
     * @return Created user
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.createUser(userDto.toEntity());
            return ResponseEntity.ok(ApiResponse.success("User created successfully", UserDto.fromEntity(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error creating user"));
        }
    }

    /**
     * Update user (admin only)
     *
     * @param id      User ID
     * @param userDto User data to update
     * @return Updated user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto) {
        try {
            userDto.setId(id);
            User updatedUser = userService.updateUser(id, userDto.toEntity());
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", UserDto.fromEntity(updatedUser)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete user (admin only)
     *
     * @param id User ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

package com.testit.reports.controller;

import com.testit.reports.controller.dto.ApiResponse;
import com.testit.reports.controller.dto.JwtAuthResponse;
import com.testit.reports.controller.dto.LoginRequest;
import com.testit.reports.model.entity.User;
import com.testit.reports.security.JwtTokenProvider;
import com.testit.reports.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    /**
     * Login user
     *
     * @param loginRequest Login request
     * @return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication.getName(), authentication.getAuthorities());

            User user = userService.getUserByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            JwtAuthResponse response = JwtAuthResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid username or password"));
        }
    }

    // Registration endpoint removed - users can only be created by admin
}

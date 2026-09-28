package com.fae.adminportal.domain.auth.controller.api;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.domain.auth.dto.JwtAuthResponse;
import com.fae.adminportal.domain.auth.dto.LoginRequest;
import com.fae.adminportal.domain.auth.dto.SignupRequest;
import com.fae.adminportal.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for authentication.
 * Base path: /api/v1/auth
 * <p>
 * Stateless — returns a JWT for subsequent requests.
 * These routes are {@code permitAll()} in the API {@code SecurityFilterChain}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT-based authentication endpoints")
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login",
               description = "Authenticates credentials and returns a JWT Bearer token.")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/signup")
    @Operation(summary = "User Signup",
               description = "Creates a user with the default 'member' role and returns a JWT Bearer token.")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully",
                                     authService.signup(request)));
    }
}
package com.fae.adminportal.domain.auth.controller;

import com.fae.adminportal.common.dto.ApiResponse;
import com.fae.adminportal.domain.auth.dto.JwtAuthResponse;
import com.fae.adminportal.domain.auth.dto.LoginRequest;
import com.fae.adminportal.domain.auth.dto.SignupRequest;
import com.fae.adminportal.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token operations")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login",
               description = "Authenticates user credentials and returns a JWT Bearer token.")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(loginRequest)));
    }

    @PostMapping("/signup")
    @Operation(summary = "User Signup",
               description = "Creates a new user with the default 'member' role and returns a JWT Bearer token.")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> signup(
            @Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully", authService.signup(signupRequest)));
    }
}
package dev.guillemdiaz.herald.controller;

import dev.guillemdiaz.herald.dto.AuthRequest;
import dev.guillemdiaz.herald.dto.AuthResponse;
import dev.guillemdiaz.herald.dto.RegisterRequest;
import dev.guillemdiaz.herald.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for registering and " +
        "logging in tenants")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new tenant", description = "Creates a " +
            "new tenant and returns a JWT token.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new tenant: {}", request.companyName());
        String token = authService.register(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Operation(summary = "Login an existing tenant", description =
            "Authenticates a tenant and returns a JWT token.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Tenant attempting login: {}", request.email());
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.dto.AuthResponse;
import com.saiteja.portfolio_backend.dto.LoginRequest;
import com.saiteja.portfolio_backend.dto.RefreshTokenRequest;
import com.saiteja.portfolio_backend.dto.RegisterRequest;
import com.saiteja.portfolio_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(authService.refreshToken(request));
    }

}

package com.saiteja.portfolio_backend.controller.auth;

import com.saiteja.portfolio_backend.dto.*;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.service.auth.AuthService;
import com.saiteja.portfolio_backend.service.auth.EmailService;
import com.saiteja.portfolio_backend.service.auth.JwtService;
import com.saiteja.portfolio_backend.service.auth.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @PostMapping("/register")
    public OtpResponse register(@RequestBody RegisterRequest request) {
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


    @PostMapping("/register/verify-otp")
    public ResponseEntity<AuthResponse> verifyRegistrationOtp(
            @RequestBody OtpVerifyRequest request) {

        User user = registrationService.completeRegistration(
                request.getEmail(),
                request.getOtp()
        );

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getRole()
        );

        String refreshToken = jwtService.generateRefreshToken(
                user.getEmail()
        );

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .message("USER_REGISTRATION_SUCCESSFUL")
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .role(user.getRole())
                        .build()
        );
    }

}

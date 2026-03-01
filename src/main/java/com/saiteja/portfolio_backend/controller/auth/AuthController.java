package com.saiteja.portfolio_backend.controller.auth;

import com.saiteja.portfolio_backend.dto.*;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.service.auth.AuthService;
import com.saiteja.portfolio_backend.service.auth.EmailService;
import com.saiteja.portfolio_backend.service.auth.JwtService;
import com.saiteja.portfolio_backend.service.auth.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @PostMapping("/register")
    public OtpResponse register(@RequestBody RegisterRequest request) {
        logger.info("Register endpoint called for email: {}", request.getEmail());
        OtpResponse response = authService.register(request);
        logger.info("Register endpoint response: {} - Email: {}", response.getMessage(), request.getEmail());
        return response;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        logger.info("Login endpoint called for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        logger.info("Login endpoint success for email: {} - Role: {}", request.getEmail(), response.getRole());
        return response;
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        logger.debug("Token refresh endpoint called");
        ResponseEntity<AuthResponse> response = ResponseEntity.ok(authService.refreshToken(request));
        logger.info("Token refresh endpoint success");
        return response;
    }


    @PostMapping("/register/verify-otp")
    public ResponseEntity<AuthResponse> verifyRegistrationOtp(
            @RequestBody OtpVerifyRequest request) {

        logger.info("OTP verification endpoint called for email: {}", request.getEmail());

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

        logger.info("OTP verification successful for email: {} - Role: {}",
            user.getEmail(), user.getRole());

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

package com.saiteja.portfolio_backend.service.auth;

import com.saiteja.portfolio_backend.dto.*;
import com.saiteja.portfolio_backend.exceptions.InvalidCredentialsException;
import com.saiteja.portfolio_backend.exceptions.UserAlreadyExistsException;
import com.saiteja.portfolio_backend.exceptions.UserNotFoundException;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegistrationService registrationService;
    private final EmailService emailService;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public OtpResponse register(RegisterRequest request) {

        if (request.getRole() != Role.PROFESSIONAL &&
                request.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("Invalid role selected");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User already exists. Please login."
            );
        }

        String otp = registrationService.initiateRegistration(request, String.valueOf(request.getRole()));

        try {
            if (!activeProfile.equals("dev")) {
                emailService.sendOtp(request.getEmail(), otp);
            }
        } catch (Exception e){
            return OtpResponse.builder()
                    .message("OTP_SENDING_FAILED")
                    .error(e.getMessage())
                    .build();
        }

        return  OtpResponse.builder()
                .message("OTP_SENT_SUCCESSFULLY")
                .error("")
                .build();

    }


    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .message("USER_LOGIN_SUCCESSFUL")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        // Validate token signature + expiration
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token expired");
        }

        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Optional: rotate refresh token (recommended)
        String newAccessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getRole()
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                user.getEmail()
        );

        return AuthResponse.builder()
                .message("TOKEN_REFRESH_SUCCESSFUL")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRole())
                .build();
    }


}

package com.saiteja.portfolio_backend.service.auth;

import com.saiteja.portfolio_backend.dto.*;
import com.saiteja.portfolio_backend.exceptions.InvalidCredentialsException;
import com.saiteja.portfolio_backend.exceptions.UserAlreadyExistsException;
import com.saiteja.portfolio_backend.exceptions.UserNotFoundException;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegistrationService registrationService;
    private final EmailService emailService;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public OtpResponse register(RegisterRequest request) {

        logger.info("User registration attempt for email: {}, role: {}", request.getEmail(), request.getRole());

        if (request.getRole() != Role.PROFESSIONAL &&
                request.getRole() != Role.RECRUITER) {
            logger.warn("Invalid role selected: {} for email: {}", request.getRole(), request.getEmail());
            throw new IllegalArgumentException("Invalid role selected");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("User already exists for email: {}", request.getEmail());
            throw new UserAlreadyExistsException(
                    "User already exists. Please login."
            );
        }

        logger.debug("Initiating registration for email: {}", request.getEmail());
        String otp = registrationService.initiateRegistration(request, String.valueOf(request.getRole()));

        try {
            if (!activeProfile.equals("dev")) {
                logger.debug("Sending OTP to email: {}", request.getEmail());
                emailService.sendOtp(request.getEmail(), otp);
                logger.info("OTP sent successfully to email: {}", request.getEmail());
            } else {
                logger.debug("Dev profile active - skipping email OTP send for: {}", request.getEmail());
            }
        } catch (Exception e){
            logger.error("Failed to send OTP to email: {} - Error: {}", request.getEmail(), e.getMessage(), e);
            return OtpResponse.builder()
                    .message("OTP_SENDING_FAILED")
                    .error(e.getMessage())
                    .build();
        }

        logger.info("Registration OTP process completed successfully for email: {}", request.getEmail());
        return  OtpResponse.builder()
                .message("OTP_SENT_SUCCESSFULLY")
                .error("")
                .build();

    }


    public AuthResponse login(LoginRequest request) {

        logger.info("Login attempt for email: {}", request.getEmail());

        long startTime = System.currentTimeMillis();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("User not found for email: {}", request.getEmail());
                    return new UserNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        logger.debug("Password validation successful for email: {}", request.getEmail());

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        long duration = System.currentTimeMillis() - startTime;
        logger.info("User login successful for email: {} - Role: {} - Duration: {}ms",
            user.getEmail(), user.getRole(), duration);

        return AuthResponse.builder()
                .message("USER_LOGIN_SUCCESSFUL")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {

        logger.debug("Token refresh requested");

        String refreshToken = request.getRefreshToken();

        // Validate token signature + expiration
        if (jwtService.isTokenExpired(refreshToken)) {
            logger.warn("Refresh token has expired");
            throw new InvalidCredentialsException("Refresh token expired");
        }

        String email = jwtService.extractEmail(refreshToken);
        logger.debug("Extracting user from refresh token for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found for refresh token - email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        // Optional: rotate refresh token (recommended)
        String newAccessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getRole()
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                user.getEmail()
        );

        logger.info("Token refreshed successfully for email: {}", email);

        return AuthResponse.builder()
                .message("TOKEN_REFRESH_SUCCESSFUL")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRole())
                .build();
    }


}

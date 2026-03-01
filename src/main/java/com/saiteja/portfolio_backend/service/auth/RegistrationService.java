package com.saiteja.portfolio_backend.service.auth;

import com.saiteja.portfolio_backend.dto.RegisterRequest;
import com.saiteja.portfolio_backend.exceptions.UserAlreadyExistsException;
import com.saiteja.portfolio_backend.model.PendingRegistration;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.PendingRegistrationRepository;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    private final PendingRegistrationRepository pendingRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public String initiateRegistration(RegisterRequest request, String role) {

        logger.info("Registration initiated for email: {} - Role: {}", request.getEmail(), role);

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration attempt for existing email: {}", request.getEmail());
            throw new UserAlreadyExistsException(
                    "User already exists. Please login."
            );
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        logger.debug("OTP generated for email: {}", request.getEmail());

        pendingRepo.deleteByEmail(request.getEmail());
        logger.debug("Cleared any existing pending registrations for email: {}", request.getEmail());

        PendingRegistration pending = PendingRegistration.builder()
                .email(request.getEmail())
                .role(role)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .otpHash(passwordEncoder.encode(otp))
                .expiryTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .build();

        pendingRepo.save(pending);
        logger.info("Pending registration created for email: {} - OTP expires in 5 minutes",
            request.getEmail());

        return otp;
    }

    public User completeRegistration(String email, String otp) {

        logger.info("OTP verification initiated for email: {}", email);

        PendingRegistration pending = pendingRepo.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("No pending registration found for email: {}", email);
                    return new RuntimeException("No registration found");
                });

        if (pending.getExpiryTime().before(new Date())) {
            logger.warn("OTP expired for email: {} - Expiry time: {}", email, pending.getExpiryTime());
            pendingRepo.deleteByEmail(email);
            throw new RuntimeException("OTP expired");
        }

        logger.debug("OTP validity check passed for email: {}", email);

        if ("dev".equalsIgnoreCase(activeProfile) && "111111".equals(otp)) {
            logger.debug("Dev profile active - OTP validation skipped for email: {}", email);
        } else {
            if (!passwordEncoder.matches(otp, pending.getOtpHash())) {
                logger.warn("Invalid OTP provided for email: {}", email);
                throw new RuntimeException("Invalid OTP");
            }
            logger.debug("OTP validation successful for email: {}", email);
        }

        User user = User.builder()
                .email(pending.getEmail())
                .password(pending.getPasswordHash())
                .role(pending.getRole())
                .build();

        userRepository.save(user);
        logger.debug("User account created for email: {} - Role: {}", email, pending.getRole());

        pendingRepo.deleteByEmail(email);
        logger.debug("Pending registration deleted for email: {}", email);

        logger.info("User registration completed successfully for email: {} - Role: {}",
            email, pending.getRole());

        return user;
    }
}

package com.saiteja.portfolio_backend.service;

import com.saiteja.portfolio_backend.dto.RegisterRequest;
import com.saiteja.portfolio_backend.exceptions.UserAlreadyExistsException;
import com.saiteja.portfolio_backend.model.PendingRegistration;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.PendingRegistrationRepository;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final PendingRegistrationRepository pendingRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public String initiateRegistration(RegisterRequest request, String role) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists. Please login."
            );
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        pendingRepo.deleteByEmail(request.getEmail());

        PendingRegistration pending = PendingRegistration.builder()
                .email(request.getEmail())
                .role(role)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .otpHash(passwordEncoder.encode(otp))
                .expiryTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .build();

        pendingRepo.save(pending);

        return otp;
    }

    public User completeRegistration(String email, String otp) {

        PendingRegistration pending = pendingRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No registration found"));

        if (pending.getExpiryTime().before(new Date())) {
            pendingRepo.deleteByEmail(email);
            throw new RuntimeException("OTP expired");
        }

        if ("dev".equalsIgnoreCase(activeProfile) && "111111".equals(otp)) {
            // skip validation
            System.out.println("Validation skipped");
        } else {
            if (!passwordEncoder.matches(otp, pending.getOtpHash())) {
                throw new RuntimeException("Invalid OTP");
            }
        }

        User user = User.builder()
                .email(pending.getEmail())
                .password(pending.getPasswordHash())
                .role(pending.getRole())
                .build();

        userRepository.save(user);

        pendingRepo.deleteByEmail(email);

        return user;
    }
}

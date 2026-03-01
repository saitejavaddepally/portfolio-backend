package com.saiteja.portfolio_backend.service.auth;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    public void sendOtp(String toEmail, String otp) {

        logger.info("Sending OTP email to: {}", toEmail);
        long startTime = System.currentTimeMillis();

        if (apiKey == null || apiKey.isBlank()) {
            logger.error("RESEND_API_KEY is not configured - Cannot send OTP to: {}", toEmail);
            throw new RuntimeException("Email service API key not configured");
        }

        logger.debug("API key is configured - proceeding with OTP send to: {}", toEmail);

        Map<String, Object> body = Map.of(
                "from", "onboarding@resend.dev",
                "to", toEmail,
                "subject", "Your OTP for Porthire",
                "html", "<h3>Your OTP is: " + otp + "</h3>"
        );

        try {
            webClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            logger.info("OTP email sent successfully to: {} - Duration: {}ms", toEmail, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to send OTP email to: {} - Error: {} - Duration: {}ms",
                toEmail, e.getMessage(), duration, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}

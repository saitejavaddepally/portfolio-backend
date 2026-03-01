package com.saiteja.portfolio_backend.controller.recruiter;

import com.saiteja.portfolio_backend.dto.UserDetailResponse;
import com.saiteja.portfolio_backend.dto.UserSummaryResponse;
import com.saiteja.portfolio_backend.service.recruiter.RecruiterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private static final Logger logger = LoggerFactory.getLogger(RecruiterController.class);

    private final RecruiterService recruiterService;

    @GetMapping("/professionals")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<UserSummaryResponse>> getProfessionals(Authentication authentication) {
        String recruiterEmail = authentication != null ? authentication.getName() : "UNKNOWN";
        logger.info("Get all professionals request from recruiter: {}", recruiterEmail);

        List<UserSummaryResponse> professionals = recruiterService.getAllProfessionals();

        logger.info("Retrieved {} professionals for recruiter: {}",
            professionals.size(), recruiterEmail);

        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/professionals/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<UserDetailResponse> getProfessionalDetails(
            @PathVariable String id,
            Authentication authentication) {

        String recruiterEmail = authentication != null ? authentication.getName() : "UNKNOWN";
        logger.info("Get professional details request from recruiter: {} for professional id: {}",
            recruiterEmail, id);

        UserDetailResponse response = recruiterService.getProfessionalDetails(id);

        logger.info("Professional details retrieved for recruiter: {} - Professional: {}",
            recruiterEmail, id);

        return ResponseEntity.ok(response);
    }
}
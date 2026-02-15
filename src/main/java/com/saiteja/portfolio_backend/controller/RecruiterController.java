package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.dto.UserDetailResponse;
import com.saiteja.portfolio_backend.dto.UserSummaryResponse;
import com.saiteja.portfolio_backend.service.RecruiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

    @GetMapping("/professionals")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<UserSummaryResponse>> getProfessionals() {
        return ResponseEntity.ok(recruiterService.getAllProfessionals());
    }

    @GetMapping("/professionals/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<UserDetailResponse> getProfessionalDetails(
            @PathVariable String id) {

        return ResponseEntity.ok(
                recruiterService.getProfessionalDetails(id)
        );
    }
}
package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public Portfolio savePortfolio(@RequestBody Map<String, Object> data) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return portfolioService.saveOrUpdatePortfolio(email, data);
    }

    @GetMapping
    public ResponseEntity<?> getPortfolio() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Portfolio portfolio = portfolioService.getPortfolio(email);

        if (portfolio == null) {
            return ResponseEntity.ok().body(null);
        }

        return ResponseEntity.ok(portfolio);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        String slug = portfolioService.publishPortfolio(email);

        return ResponseEntity.ok(Map.of(
                "message", "PORTFOLIO_PUBLISHED",
                "publicUrl", "/p/" + slug
        ));
    }



}

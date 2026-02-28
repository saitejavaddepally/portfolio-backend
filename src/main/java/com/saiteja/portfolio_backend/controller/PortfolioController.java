package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.dto.UserPrincipal;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();


        return portfolioService.saveOrUpdatePortfolio(principal.getEmail(), principal.getUserId(), data);
    }

    @GetMapping
    public ResponseEntity<?> getPortfolio() {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Portfolio portfolio = portfolioService.getPortfolio(principal.getEmail());

        if (portfolio == null) {
            return ResponseEntity.ok().body(null);
        }

        return ResponseEntity.ok(portfolio);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish() {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        String slug = portfolioService.publishPortfolio(principal.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "PORTFOLIO_PUBLISHED",
                "publicUrl", "/p/" + slug
        ));
    }



}

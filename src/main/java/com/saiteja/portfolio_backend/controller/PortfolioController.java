package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.dto.UserPrincipal;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    @PostMapping
    public Portfolio savePortfolio(@RequestBody Map<String, Object> data) {

        logger.info("Portfolio save request received");

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        logger.info("Saving portfolio for user: {} - ID: {}", principal.getEmail(), principal.getUserId());

        Portfolio portfolio = portfolioService.saveOrUpdatePortfolio(principal.getEmail(), principal.getUserId(), data);

        logger.info("Portfolio saved successfully for user: {}", principal.getEmail());

        return portfolio;
    }

    @GetMapping
    public ResponseEntity<?> getPortfolio() {

        logger.debug("Portfolio retrieval request received");

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        logger.debug("Retrieving portfolio for user: {}", principal.getEmail());

        Portfolio portfolio = portfolioService.getPortfolio(principal.getEmail());

        if (portfolio == null) {
            logger.debug("Portfolio not found for user: {}", principal.getEmail());
            return ResponseEntity.ok().body(null);
        }

        logger.debug("Portfolio retrieved successfully for user: {}", principal.getEmail());
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish() {

        logger.info("Portfolio publish request received");

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        logger.info("Publishing portfolio for user: {}", principal.getEmail());

        String slug = portfolioService.publishPortfolio(principal.getEmail());

        logger.info("Portfolio published successfully for user: {} - Slug: {}", principal.getEmail(), slug);

        return ResponseEntity.ok(Map.of(
                "message", "PORTFOLIO_PUBLISHED",
                "publicUrl", "/p/" + slug
        ));
    }


}

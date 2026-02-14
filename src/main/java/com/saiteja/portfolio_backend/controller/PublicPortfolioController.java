package com.saiteja.portfolio_backend.controller;

import com.saiteja.portfolio_backend.exceptions.PortfolioNotFoundException;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicPortfolioController {

    private final PortfolioRepository portfolioRepository;

    @GetMapping("/{slug}")
    public ResponseEntity<?> getPublicPortfolio(@PathVariable String slug) {

        Portfolio portfolio = portfolioRepository
                .findByPublicSlugAndPublishedTrue(slug)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        return ResponseEntity.ok(portfolio.getData());
    }
}


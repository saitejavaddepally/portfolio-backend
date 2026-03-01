package com.saiteja.portfolio_backend.service;

import com.saiteja.portfolio_backend.exceptions.PortfolioNotFoundException;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;

    private final AISummaryService aiSummaryService;

    public Portfolio saveOrUpdatePortfolio(String email, String userId, Map<String, Object> data) {

        logger.info("Portfolio save/update initiated for email: {} - userId: {}", email, userId);
        long startTime = System.currentTimeMillis();

        Portfolio portfolio = portfolioRepository.findByUserEmail(email)
                .orElse(
                        Portfolio.builder()
                                .userEmail(email)
                                .createdAt(Instant.now())
                                .build()
                );

        portfolio.setUserId(userId);
        portfolio.setData(data);
        portfolio.setUpdatedAt(Instant.now());

        logger.debug("Portfolio object prepared for email: {}", email);

        aiSummaryService.generateAndSaveSummary(email, userId, data);
        logger.debug("AI summary generation triggered (async) for email: {}", email);

        Portfolio saved = portfolioRepository.save(portfolio);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Portfolio saved successfully for email: {} - Duration: {}ms", email, duration);

        return saved;
    }

    public Portfolio getPortfolio(String email) {
        logger.debug("Retrieving portfolio for email: {}", email);
        Portfolio portfolio = portfolioRepository.findByUserEmail(email).orElse(null);

        if (portfolio == null) {
            logger.warn("Portfolio not found for email: {}", email);
        } else {
            logger.debug("Portfolio retrieved successfully for email: {} - Published: {}",
                email, portfolio.isPublished());
        }

        return portfolio;
    }

    public String publishPortfolio(String email) {

        logger.info("Portfolio publish initiated for email: {}", email);

        Portfolio portfolio = portfolioRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    logger.error("Portfolio not found for publishing - email: {}", email);
                    return new PortfolioNotFoundException("Portfolio not found");
                });

        if (!portfolio.isPublished()) {
            logger.debug("Portfolio not yet published, generating slug for email: {}", email);

            String slug = generateSlug(
                    portfolio.getData().get("hero") != null
                            ? ((Map<String, Object>) portfolio.getData().get("hero"))
                            .get("name").toString()
                            : email
            );

            portfolio.setPublicSlug(slug);
            portfolio.setPublished(true);
            portfolio.setUpdatedAt(Instant.now());

            portfolioRepository.save(portfolio);
            logger.info("Portfolio published successfully for email: {} - Slug: {}", email, slug);
        } else {
            logger.debug("Portfolio already published for email: {} - Slug: {}",
                email, portfolio.getPublicSlug());
        }

        return portfolio.getPublicSlug();
    }


    private String generateSlug(String base) {

        logger.debug("Generating slug from base: {}", base);

        String slug = base.toLowerCase().replaceAll("\\s+", "-");

        int counter = 1;

        while (portfolioRepository.existsByPublicSlug(slug)) {
            slug = slug + "-" + counter;
            counter++;
            logger.debug("Slug collision detected, trying: {}", slug);
        }

        logger.debug("Slug generated successfully: {}", slug);
        return slug;
    }

}

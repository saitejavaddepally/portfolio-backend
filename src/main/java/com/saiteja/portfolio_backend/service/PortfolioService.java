package com.saiteja.portfolio_backend.service;

import com.saiteja.portfolio_backend.exceptions.PortfolioNotFoundException;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    private final AISummaryService aiSummaryService;

    public Portfolio saveOrUpdatePortfolio(String email, String userId, Map<String, Object> data) {

        Portfolio portfolio = portfolioRepository.findByUserEmail(email)
                .orElse(
                        Portfolio.builder()
                                .userEmail(email)
                                .userId(userId)
                                .createdAt(Instant.now())
                                .build()
                );

        portfolio.setData(data);
        portfolio.setUpdatedAt(Instant.now());

        aiSummaryService.generateAndSaveSummary(email,data);

        return portfolioRepository.save(portfolio);
    }

    public Portfolio getPortfolio(String email) {
        return portfolioRepository.findByUserEmail(email)
                .orElse(null);
    }

    public String publishPortfolio(String email) {

        Portfolio portfolio = portfolioRepository.findByUserEmail(email)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.isPublished()) {

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
        }

        return portfolio.getPublicSlug();
    }


    private String generateSlug(String base) {

        String slug = base.toLowerCase().replaceAll("\\s+", "-");

        int counter = 1;

        while (portfolioRepository.existsByPublicSlug(slug)) {
            slug = slug + "-" + counter;
            counter++;
        }

        return slug;
    }

}

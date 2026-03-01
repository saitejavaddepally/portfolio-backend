package com.saiteja.portfolio_backend.service.recruiter;

import com.saiteja.portfolio_backend.dto.UserDetailResponse;
import com.saiteja.portfolio_backend.dto.UserSummaryResponse;
import com.saiteja.portfolio_backend.exceptions.UserNotFoundException;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.PortfolioRepository;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private static final Logger logger = LoggerFactory.getLogger(RecruiterService.class);

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public List<UserSummaryResponse> getAllProfessionals() {

        logger.info("Fetching all published professional portfolios");

        List<Portfolio> portfolios =
                portfolioRepository.findPublishedPortfolios();

        logger.debug("Found {} published portfolios", portfolios.size());

        List<String> emails = portfolios.stream()
                .map(Portfolio::getUserEmail)
                .toList();

        List<User> users = userRepository.findByEmailIn(emails);
        logger.debug("Found {} users for {} emails", users.size(), emails.size());

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getEmail,
                        u -> u
                ));

        List<UserSummaryResponse> response = portfolios.stream()
                .map(portfolio -> {

                    User user = userMap.get(portfolio.getUserEmail());

                    if (user == null) {
                        logger.warn("User not found for portfolio email: {}", portfolio.getUserEmail());
                        return null;
                    }

                    List<String> skills = List.of();
                    String name = null;

                    Map<String, Object> data = portfolio.getData();

                    if (data != null) {

                        if (data.get("skills") instanceof List<?>) {
                            skills = ((List<?>) data.get("skills"))
                                    .stream()
                                    .map(Object::toString)
                                    .filter(s -> !s.isBlank())
                                    .toList();
                        }

                        if (data.get("hero") instanceof Map<?, ?> heroMap) {
                            Object nameObj = heroMap.get("name");
                            if (nameObj != null) {
                                name = nameObj.toString();
                            }
                        }
                    }

                    return UserSummaryResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .skills(skills)
                            .name(name)
                            .isPublished(true)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        logger.info("Returning {} professional summaries", response.size());
        return response;
    }

    public UserDetailResponse getProfessionalDetails(String userId) {

        logger.debug("Fetching professional details for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for userId: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        if (!"PROFESSIONAL".equals(user.getRole())) {
            logger.warn("Invalid user type for userId: {} - Role: {}", userId, user.getRole());
            throw new RuntimeException("Invalid user type");
        }

        Portfolio portfolio = portfolioRepository
                .findByUserEmail(user.getEmail())
                .orElse(null);

        if (portfolio == null) {
            logger.warn("Portfolio not found for professional userId: {} - Email: {}",
                userId, user.getEmail());
        } else {
            logger.debug("Portfolio found for professional userId: {}", userId);
        }

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .portfolio(portfolio)
                .build();
    }
}
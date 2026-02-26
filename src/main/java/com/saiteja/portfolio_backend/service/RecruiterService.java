package com.saiteja.portfolio_backend.service;

import com.saiteja.portfolio_backend.dto.UserDetailResponse;
import com.saiteja.portfolio_backend.dto.UserSummaryResponse;
import com.saiteja.portfolio_backend.exceptions.UserNotFoundException;
import com.saiteja.portfolio_backend.model.Portfolio;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.PortfolioRepository;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public List<UserSummaryResponse> getAllProfessionals() {

        List<Portfolio> portfolios =
                portfolioRepository.findPublishedPortfolios();

        List<String> emails = portfolios.stream()
                .map(Portfolio::getUserEmail)
                .toList();

        List<User> users = userRepository.findByEmailIn(emails);

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(
                        User::getEmail,
                        u -> u
                ));

        // 4️⃣ Build response
        return portfolios.stream()
                .map(portfolio -> {

                    User user = userMap.get(portfolio.getUserEmail());

                    if (user == null) {
                        return null; // should not happen ideally
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
                            .id(user.getId()) // ✅ USER ID (FIXED)
                            .email(user.getEmail())
                            .skills(skills)
                            .name(name)
                            .isPublished(true)
                            .build();
                })
                .filter(response -> response != null)
                .toList();
    }

    public UserDetailResponse getProfessionalDetails(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!"PROFESSIONAL".equals(user.getRole())) {
            throw new RuntimeException("Invalid user type");
        }

        Portfolio portfolio = portfolioRepository
                .findByUserEmail(user.getEmail())
                .orElse(null);

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .portfolio(portfolio)
                .build();
    }
}
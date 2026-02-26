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

        List<User> professionals = userRepository.findByRole("PROFESSIONAL");

        List<String> emails = professionals.stream()
                .map(User::getEmail)
                .toList();

        List<Portfolio> portfolios =
                portfolioRepository.findSkillsByUserEmailIn(emails);

        Map<String, Portfolio> portfolioMap = portfolios.stream()
                .collect(Collectors.toMap(
                        Portfolio::getUserEmail,
                        p -> p
                ));

        return professionals.stream()
                .map(user -> {

                    Portfolio portfolio = portfolioMap.get(user.getEmail());

                    List<String> skills = List.of();

                    if (portfolio != null &&
                            portfolio.getData() != null &&
                            portfolio.getData().get("skills") instanceof List<?>) {

                        skills = ((List<?>) portfolio.getData().get("skills"))
                                .stream()
                                .map(Object::toString)
                                .toList();
                    }

                    return UserSummaryResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .skills(skills)
                            .build();
                })
                .toList();
    }

    public UserDetailResponse getProfessionalDetails(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getRole().equals("PROFESSIONAL")) {
            throw new RuntimeException("Invalid user type");
        }

        var portfolio = portfolioRepository.findByUserEmail(user.getEmail())
                .orElse(null);

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .portfolio(portfolio)
                .build();
    }
}
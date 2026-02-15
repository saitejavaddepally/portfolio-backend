package com.saiteja.portfolio_backend.service;

import com.saiteja.portfolio_backend.dto.UserDetailResponse;
import com.saiteja.portfolio_backend.dto.UserSummaryResponse;
import com.saiteja.portfolio_backend.exceptions.UserNotFoundException;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.PortfolioRepository;
import com.saiteja.portfolio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public List<UserSummaryResponse> getAllProfessionals() {

        return userRepository.findByRole("PROFESSIONAL")
                .stream()
                .map(user -> UserSummaryResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .userData(getProfessionalDetails(user.getId()))
                        .build())
                .collect(Collectors.toList());
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

package com.saiteja.portfolio_backend.security;

import com.saiteja.portfolio_backend.dto.UserPrincipal;
import com.saiteja.portfolio_backend.model.User;
import com.saiteja.portfolio_backend.repository.UserRepository;
import com.saiteja.portfolio_backend.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.debug("Processing request: {} - Method: {}", requestPath, request.getMethod());

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid Authorization header found for request: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            final String email = jwtService.extractEmail(token);
            logger.debug("Extracted email from token: {} - Request: {}", email, requestPath);

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                var userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    if (jwtService.isTokenValid(token, email)) {
                        logger.debug("JWT token validation successful for email: {} - Request: {}",
                            email, requestPath);

                        UsernamePasswordAuthenticationToken authToken =
                                getUsernamePasswordAuthenticationToken(userOptional.get());

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authToken);

                        logger.info("User authenticated successfully - Email: {} - Request: {} - Role: {}",
                            email, requestPath, userOptional.get().getRole());
                    } else {
                        logger.warn("JWT token validation failed for email: {} - Request: {}",
                            email, requestPath);
                    }
                } else {
                    logger.warn("User not found for email: {} - Token extraction failed - Request: {}",
                        email, requestPath);
                }
            }

        } catch (Exception e) {
            logger.error("Exception occurred during JWT authentication for request: {} - Error: {}",
                requestPath, e.getMessage());
            logger.trace("Full stack trace:", e);
        }

        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(User user) {

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
    }
}

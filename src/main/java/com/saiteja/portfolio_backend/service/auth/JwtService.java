package com.saiteja.portfolio_backend.service.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final Key signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        logger.debug("Initializing JWT service with signing key");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String email, String role) {
        logger.debug("Generating access token for email: {} - Role: {}", email, role);
        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("Access token generated successfully for email: {}", email);
        return token;
    }

    public String generateRefreshToken(String email) {
        logger.debug("Generating refresh token for email: {}", email);
        String token = Jwts.builder()
                .setSubject(email)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("Refresh token generated successfully for email: {}", email);
        return token;
    }

    public String extractEmail(String token) {
        logger.trace("Extracting email from token");
        String email = extractAllClaims(token).getSubject();
        logger.debug("Email extracted from token: {}", email);
        return email;
    }

    public String extractRole(String token) {
        logger.trace("Extracting role from token");
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        logger.trace("Extracting token type from token");
        return extractAllClaims(token).get("type", String.class);
    }

    public boolean isTokenExpired(String token) {
        boolean expired = extractAllClaims(token)
                .getExpiration()
                .before(new Date());
        logger.debug("Token expiration check result: {}", expired);
        return expired;
    }

    public boolean isTokenValid(String token, String email) {
        boolean valid = extractEmail(token).equals(email) && !isTokenExpired(token);
        logger.debug("Token validation result for email: {} - Valid: {}", email, valid);
        return valid;
    }

    private Claims extractAllClaims(String token) {
        try {
            logger.trace("Parsing JWT token claims");
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.warn("Token format not supported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage(), e);
            throw e;
        }
    }
}

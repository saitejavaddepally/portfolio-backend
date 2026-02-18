package com.saiteja.portfolio_backend.repository;

import com.saiteja.portfolio_backend.model.AISummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AISummaryRepository extends MongoRepository<AISummary, String> {
    Optional<AISummary> findByUserEmail(String userEmail);
}
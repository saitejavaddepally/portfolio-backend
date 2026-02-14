package com.saiteja.portfolio_backend.repository;

import com.saiteja.portfolio_backend.model.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PortfolioRepository extends MongoRepository<Portfolio, String> {

    Optional<Portfolio> findByUserEmail(String userEmail);

    Optional<Portfolio> findByPublicSlugAndPublishedTrue(String publicSlug);

    boolean existsByPublicSlug(String publicSlug);


}

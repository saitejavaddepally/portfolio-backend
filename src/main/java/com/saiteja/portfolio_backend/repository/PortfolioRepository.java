package com.saiteja.portfolio_backend.repository;

import com.saiteja.portfolio_backend.model.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends MongoRepository<Portfolio, String> {

    Optional<Portfolio> findByUserEmail(String userEmail);

    Optional<Portfolio> findByPublicSlugAndPublishedTrue(String publicSlug);

    boolean existsByPublicSlug(String publicSlug);


    List<Portfolio> findByUserEmailIn(List<String> emails);

    @Query(value = "{ 'userEmail': { $in: ?0 } }",
            fields = "{ 'userEmail': 1, 'data.skills': 1, 'published': 1, 'publicSlug': 1 }")
    List<Portfolio> findSkillsByUserEmailIn(List<String> emails);
}

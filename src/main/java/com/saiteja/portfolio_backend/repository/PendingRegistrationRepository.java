package com.saiteja.portfolio_backend.repository;

import com.saiteja.portfolio_backend.model.PendingRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PendingRegistrationRepository
        extends MongoRepository<PendingRegistration, String> {

    Optional<PendingRegistration> findByEmail(String email);

    void deleteByEmail(String email);
}

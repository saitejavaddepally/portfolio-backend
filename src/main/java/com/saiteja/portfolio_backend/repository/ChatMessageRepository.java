package com.saiteja.portfolio_backend.repository;

import com.saiteja.portfolio_backend.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByRecruiterEmailAndCandidateEmailOrderByCreatedAtAsc(
            String recruiterEmail,
            String candidateEmail
    );

    List<ChatMessage> findTop4ByRecruiterEmailAndCandidateEmailOrderByCreatedAtDesc(
            String recruiterEmail,
            String candidateEmail
    );
}
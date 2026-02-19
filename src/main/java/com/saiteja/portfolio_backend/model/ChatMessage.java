package com.saiteja.portfolio_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    private String recruiterEmail;
    private String candidateEmail;

    private String role; // "user" or "assistant"

    private String content;

    private Instant createdAt;
}
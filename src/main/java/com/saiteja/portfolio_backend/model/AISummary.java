package com.saiteja.portfolio_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_summaries")
public class AISummary {

    @Id
    private String id;
    private String userEmail;
    private String model;
    private Map<String, Object> structuredSummary;
    private Instant createdAt;
    private Instant updatedAt;
}
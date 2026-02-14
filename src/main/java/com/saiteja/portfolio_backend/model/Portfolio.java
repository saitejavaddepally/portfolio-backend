package com.saiteja.portfolio_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "portfolios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    private String id;

    private String userEmail;

    private Map<String, Object> data;

    private boolean published;

    private String publicSlug;

    private Instant createdAt;
    private Instant updatedAt;
}



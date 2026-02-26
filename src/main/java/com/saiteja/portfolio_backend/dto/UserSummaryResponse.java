package com.saiteja.portfolio_backend.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {

    private String id;
    private String email;
    private String role;
    private List<String> skills;
    private Double matchScore;
    private boolean isPublished;
    private String publicSlug;
    private String name;
}
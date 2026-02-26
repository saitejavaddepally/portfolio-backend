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

    private List<String> skills; // lightweight

    private Double matchScore; // nullable for now
}
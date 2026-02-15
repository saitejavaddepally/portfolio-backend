package com.saiteja.portfolio_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailResponse {

    private String id;
    private String email;
    private String role;
    private Object portfolio; // later we can create proper DTO
}
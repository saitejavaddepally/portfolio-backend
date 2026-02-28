package com.saiteja.portfolio_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {

    private String userId;
    private String email;
    private String role;
}
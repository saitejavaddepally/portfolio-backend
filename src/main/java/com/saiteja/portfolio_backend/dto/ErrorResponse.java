package com.saiteja.portfolio_backend.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    String errorCode;
    String errorMessage;
    String statusCode;
}

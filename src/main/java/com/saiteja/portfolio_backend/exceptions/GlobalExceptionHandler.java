package com.saiteja.portfolio_backend.exceptions;

import com.saiteja.portfolio_backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setErrorCode("USER_NOT_FOUND");
        error.setErrorMessage(ex.getMessage());
        error.setStatusCode(String.valueOf(HttpStatus.NOT_FOUND.value()));

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setErrorCode("INVALID_CREDENTIALS");
        error.setErrorMessage(ex.getMessage());
        error.setStatusCode(String.valueOf(HttpStatus.UNAUTHORIZED.value()));

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorResponse error = new ErrorResponse();
        error.setErrorCode("INTERNAL_SERVER_ERROR");
        error.setErrorMessage(ex.getMessage());
        error.setStatusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePortfolioNotFound(
            PortfolioNotFoundException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setErrorCode("PORTFOLIO_NOT_FOUND");
        error.setErrorMessage(ex.getMessage());
        error.setStatusCode(String.valueOf(HttpStatus.NOT_FOUND.value()));

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}
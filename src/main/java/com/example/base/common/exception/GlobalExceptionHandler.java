package com.example.base.common.exception;

import com.example.base.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBiz(BizException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
        FieldError fieldError = null;
        if (ex instanceof MethodArgumentNotValidException manex) {
            fieldError = manex.getBindingResult().getFieldError();
        } else if (ex instanceof BindException bex) {
            fieldError = bex.getBindingResult().getFieldError();
        }
        String message = fieldError != null
                ? fieldError.getField() + " " + Objects.requireNonNullElse(fieldError.getDefaultMessage(), "invalid")
                : "Validation failed";
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_ARGUMENT", message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication exception on {}: {}", request.getRequestURI(), ex.getMessage());
        String code = switch (ex) {
            case LockedException ignored -> "ACCOUNT_LOCKED";
            case BadCredentialsException ignored -> "INVALID_CREDENTIALS";
            default -> "UNAUTHORIZED";
        };
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(code, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternal(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("INTERNAL_ERROR", "Internal server error"));
    }
}

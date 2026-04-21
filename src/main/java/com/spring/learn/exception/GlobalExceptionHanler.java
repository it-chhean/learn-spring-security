package com.spring.learn.exception;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHanler {

    private ProblemDetail createDetail(HttpStatus status, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                        ));

        ProblemDetail detail = createDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        detail.setProperty("errors", fieldErrors);
        return detail;
    }

    @ExceptionHandler({
    IllegalStateException.class, IllegalArgumentException.class
    })
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        return createDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
    BadCredentialsException.class, UsernameNotFoundException.class
    })
    public ProblemDetail handleBadCredentails(RuntimeException ex) {
        return createDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleBadCredentails(AccountStatusException ex) {
        return createDetail(HttpStatus.UNAUTHORIZED, "Account is locked or disabled!");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleBadCredentails(AccessDeniedException ex) {
        return createDetail(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    /*
    @ExceptionHandler({
    ExpiredJwtException.class, SignatureException.class
    })
    public ProblemDetail handleJwtException(RuntimeException ex) {
        String msg = (ex instanceof ExpiredJwtException) ? "JWT token has expired" : "Invalid JWT signature";
        return createDetail(HttpStatus.UNAUTHORIZED, msg);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        return createDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }*/

}

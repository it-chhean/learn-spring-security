package com.spring.learn.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String message;
    private LocalDateTime createdAt;
}

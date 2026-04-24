package com.spring.learn.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(

        @NotBlank
        String fullName,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password

) {}

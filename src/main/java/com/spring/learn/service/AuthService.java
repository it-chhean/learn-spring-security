package com.spring.learn.service;

import com.spring.learn.dto.LoginRequest;
import com.spring.learn.dto.LoginResponse;
import com.spring.learn.dto.RegisterRequest;
import com.spring.learn.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}

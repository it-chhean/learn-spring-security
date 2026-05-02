package com.spring.learn.service.Impl;

import com.spring.learn.dto.LoginRequest;
import com.spring.learn.dto.LoginResponse;
import com.spring.learn.dto.RegisterRequest;
import com.spring.learn.dto.RegisterResponse;
import com.spring.learn.entity.Role;
import com.spring.learn.entity.User;
import com.spring.learn.exception.PasswordMismatchException;
import com.spring.learn.exception.RoleNotFoundException;
import com.spring.learn.exception.UserAlreadyExistsException;
import com.spring.learn.repository.RoleRepository;
import com.spring.learn.repository.UserRepository;
import com.spring.learn.service.AuthService;
import com.spring.learn.service.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        log.info("Register new user: {}", request.getUsername());

        // 1. check password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Password not match");
        }

        // 2. check username is not token
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists:" + request.getUsername());
        }

        // check email is match
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        Role role = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User saved = userRepository.save(newUser);
        return RegisterResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .createdAt(saved.getCreatedAt())
                .message("Registration successfully please log in.")
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + request.getUsername())
                );

        String token = jwtUtil.generate(user.getUsername());
        log.info("Login successfully for user: {}", request.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .message("Login Successfully")
                .build();
    }

}

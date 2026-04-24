package com.spring.learn.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spring.learn.security.JwtService;
import com.spring.learn.user.Role;
import com.spring.learn.user.User;
import com.spring.learn.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponsDto register(AuthRequestDto requestDto) {

        if (userRepository.existsByEmail(requestDto.email())) {
            throw new IllegalStateException(
                    "Email already in use: " + requestDto.email());
        }

        User user = new User().builder()
            .fullName(requestDto.fullName())
            .email(requestDto.email())
            .password(passwordEncoder.encode(requestDto.password()))
            .role(Role.USER)
            .enabled(true)
            .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponsDto(token);
    }
}

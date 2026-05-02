package com.spring.learn.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isExpired(String token) {
        return getClaims(token).getExpiration().after(new Date());
    }

    public boolean validate(String token) {
        try {
            return getUsername(token) != null && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String generate(String usename) {
        return Jwts.builder()
                .subject(usename)
                .issuer("it-chhean.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()))
                .signWith(secretKey)
                .compact();
    }
}
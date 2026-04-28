package com.spring.learn.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService  jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull filterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {

            userEmail = jwtService.extractUsername(jwt);

        } catch (Exception e) {

            filterChain.doFilter(request, response);
            return;

        }

        if (userEmail != null && 
            SecurityContextHolder.getContext().getAuthentication() == null ) {
            
            UserDetails userDetails = 
                this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                            );
    
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                            .buildDetails(request));
    
                SecurityContextHolder
                    .getContext().setAuthentication(authToken);
    
            }
        }
    
        filterChain.doFilter(request, response);
    }
}

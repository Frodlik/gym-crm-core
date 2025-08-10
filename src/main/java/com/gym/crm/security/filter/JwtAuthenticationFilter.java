package com.gym.crm.security.filter;

import com.gym.crm.security.service.CustomUserDetailsService;
import com.gym.crm.security.JwtTokenHandler;
import com.gym.crm.util.TokenExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenHandler jwtTokenHandler;
    private final CustomUserDetailsService userDetailsService;
    private final TokenExtractor tokenExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        tokenExtractor.extractAccessToken(request)
                .filter(this::isValidAccessToken)
                .ifPresent(token -> setAuthenticationFromToken(token, request));

        chain.doFilter(request, response);
    }

    private boolean isValidAccessToken(String token) {
        try {
            return extractUsernameFromToken(token)
                    .map(username -> jwtTokenHandler.validateAccessToken(token, username))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private Optional<String> extractUsernameFromToken(String token) {
        try {
            return Optional.ofNullable(jwtTokenHandler.getUsernameFromToken(token));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void setAuthenticationFromToken(String token, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        extractUsernameFromToken(token)
                .ifPresent(username -> authenticateUser(username, request));
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            log.warn("Failed to authenticate user {}: {}", username, e.getMessage());
        }
    }
}

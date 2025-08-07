package com.gym.crm.security.filter;

import com.gym.crm.security.CustomUserDetailsService;
import com.gym.crm.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.debug("Processing JWT authentication for: {}", request.getRequestURI());

        String token = extractTokenFromRequest(request);

        if (token != null && isTokenValid(token)) {
            setAuthenticationFromToken(token, request);
        }

        chain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return extractTokenFromCookies(request);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> jwtCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean isTokenValid(String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);

            return username != null && jwtTokenUtil.validateToken(token, username);
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());

            return false;
        }
    }

    private void setAuthenticationFromToken(String token, HttpServletRequest request) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("User {} authenticated successfully", username);
        } catch (Exception e) {
            log.warn("Failed to set authentication: {}", e.getMessage());
        }
    }
}

package com.gym.crm.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationContext {
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    public Optional<String> getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    public Optional<String> getCurrentUserType() {
        return getCurrentUsername()
                .flatMap(this::getUserTypeByUsername)
                .or(this::getUserTypeFromRequest);
    }

    public boolean isCurrentUserTrainee() {
        return getCurrentUserType()
                .map("TRAINEE"::equals)
                .orElse(false);
    }

    public boolean isCurrentUserTrainer() {
        return getCurrentUserType()
                .map("TRAINER"::equals)
                .orElse(false);
    }

    public Optional<String> extractTokenFromRequest() {
        return getCurrentRequest()
                .flatMap(this::extractTokenFromRequest);
    }

    private Optional<String> getUserTypeByUsername(String username) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            return Optional.of(userDetails.getRole());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> getUserTypeFromRequest() {
        return getCurrentRequest()
                .flatMap(this::extractUserTypeFromRequest);
    }

    private Optional<String> extractUserTypeFromRequest(HttpServletRequest request) {
        return Optional.ofNullable((String) request.getAttribute("userType"))
                .or(() -> extractUserTypeFromSession(request));
    }

    private Optional<String> extractUserTypeFromSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false))
                .map(session -> (String) session.getAttribute("userType"));
    }

    private Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        return extractBearerToken(request)
                .or(() -> extractTokenFromCookies(request));
    }

    private Optional<String> extractBearerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    private Optional<String> extractTokenFromCookies(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> jwtCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private Optional<HttpServletRequest> getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }
}

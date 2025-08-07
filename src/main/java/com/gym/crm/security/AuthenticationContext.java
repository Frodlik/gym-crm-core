package com.gym.crm.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthenticationContext {
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    public String getCurrentUserType() {
        String username = getCurrentUsername();
        if (username == null) {
            return getUserTypeFromRequest();
        }

        try {
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            return userDetails.getRole();
        } catch (Exception e) {
            return getUserTypeFromRequest();
        }
    }

    public boolean isCurrentUserTrainee() {
        return "TRAINEE".equals(getCurrentUserType());
    }

    public boolean isCurrentUserTrainer() {
        return "TRAINER".equals(getCurrentUserType());
    }

    public String extractTokenFromRequest() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> jwtCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String getUserTypeFromRequest() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String userType = (String) request.getAttribute("userType");
        if (userType != null) {
            return userType;
        }

        if (request.getSession(false) != null) {
            return (String) request.getSession(false).getAttribute("userType");
        }

        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        return servletAttributes.getRequest();
    }
}

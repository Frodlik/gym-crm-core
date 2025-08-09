package com.gym.crm.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TokenExtractor {
    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Value("${jwt.refresh.cookie.name}")
    private String refreshCookieName;

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractBearerToken(request)
                .or(() -> extractTokenFromCookies(request, jwtCookieName));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractTokenFromCookies(request, refreshCookieName);
    }

    public Optional<String> extractAccessTokenFromCurrentRequest() {
        return getCurrentRequest()
                .flatMap(this::extractAccessToken);
    }

    public Optional<String> extractRefreshTokenFromCurrentRequest() {
        return getCurrentRequest()
                .flatMap(this::extractRefreshToken);
    }

    private Optional<String> extractBearerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    private Optional<String> extractTokenFromCookies(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> cookieName.equals(cookie.getName()))
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
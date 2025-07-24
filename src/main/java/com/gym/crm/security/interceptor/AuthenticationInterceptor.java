package com.gym.crm.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/login",
            "/trainees/register",
            "/trainers/register"
    );
    private static final List<String> REGISTRATION_PATHS = Arrays.asList(
            "/trainees",
            "/trainers"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        String path = requestPath.replaceFirst("^/api", "");
        if (isPublicPath(path)) {
            return true;
        }

        if ("POST".equals(method) && isRegistrationPath(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication required. Please login first.\"}");

            return false;
        }

        request.setAttribute("currentUsername", session.getAttribute("username"));
        request.setAttribute("currentUserType", session.getAttribute("userType"));

        return true;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::contains);
    }

    private boolean isRegistrationPath(String path) {
        return REGISTRATION_PATHS.stream().anyMatch(path::equals);
    }
}

package com.gym.crm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthenticationContext {
    public String getCurrentUserType() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String userType = (String) request.getAttribute("userType");
        if (userType != null) {
            return userType;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (String) session.getAttribute("userType");
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        return servletAttributes.getRequest();
    }
}

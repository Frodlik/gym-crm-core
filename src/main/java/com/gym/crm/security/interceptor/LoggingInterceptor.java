package com.gym.crm.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String transactionId = UUID.randomUUID().toString();

        MDC.put(TRANSACTION_ID_KEY, transactionId);

        request.setAttribute(TRANSACTION_ID_KEY, transactionId);

        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        logger.info("!INCOMING REQUEST! TransactionId: {} | Method: {} | URI: {} | Client IP: {} | User-Agent: {}",
                transactionId, method, fullUrl, clientIp, userAgent);

        if (logger.isDebugEnabled()) {
            logRequestHeaders(request, transactionId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        try {
            String transactionId = (String) request.getAttribute(TRANSACTION_ID_KEY);
            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);

            if (transactionId == null || startTime == null) {
                return;
            }

            logRequestCompletion(request, response, ex, transactionId, startTime);

        } finally {
            MDC.clear();
        }
    }

    private void logRequestCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex,
                                      String transactionId, Long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int statusCode = response.getStatus();
        String statusMessage = getStatusMessage(statusCode);

        if (ex != null) {
            logger.error("!REQUEST COMPLETED WITH ERROR! TransactionId: {} | Method: {} | URI: {} | Status: {} | Duration: {}ms | Error: {}",
                    transactionId, method, uri, statusCode, duration, ex.getMessage(), ex);
        } else if (statusCode >= 400) {
            logger.warn("!REQUEST COMPLETED! TransactionId: {} | Method: {} | URI: {} | Status: {} ({}) | Duration: {}ms",
                    transactionId, method, uri, statusCode, statusMessage, duration);
        } else {
            logger.info("!REQUEST COMPLETED! TransactionId: {} | Method: {} | URI: {} | Status: {} ({}) | Duration: {}ms",
                    transactionId, method, uri, statusCode, statusMessage, duration);
        }

        if (logger.isDebugEnabled()) {
            logResponseHeaders(response, transactionId);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    private void logRequestHeaders(HttpServletRequest request, String transactionId) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            if (!isSensitiveHeader(headerName)) {
                headers.append(headerName).append(": ").append(headerValue).append(" | ");
            }
        });

        if (!headers.isEmpty()) {
            logger.debug("REQUEST HEADERS - TransactionId: {} | Headers: {}", transactionId, headers);
        }
    }

    private void logResponseHeaders(HttpServletResponse response, String transactionId) {
        StringBuilder headers = new StringBuilder();
        response.getHeaderNames().forEach(headerName -> {
            String headerValue = response.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append(" | ");
        });

        if (!headers.isEmpty()) {
            logger.debug("RESPONSE HEADERS - TransactionId: {} | Headers: {}", transactionId, headers);
        }
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseHeader = headerName.toLowerCase();

        return lowerCaseHeader.contains("authorization") ||
                lowerCaseHeader.contains("cookie") ||
                lowerCaseHeader.contains("password") ||
                lowerCaseHeader.contains("token");
    }
}

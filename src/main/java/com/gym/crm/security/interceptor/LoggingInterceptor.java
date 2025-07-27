package com.gym.crm.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String REQUEST_START_TIME = "requestStartTime";
    private static final int MAX_PAYLOAD_LENGTH = 1000;

    private static final Set<String> SENSITIVE_ENDPOINTS = Set.of(
            "/api/v1/trainees/register",
            "/api/v1/trainers/register"
    );

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
        String contentType = request.getContentType();

        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        logger.info("!INCOMING REQUEST! TransactionId: {} | {} {} | IP: {} | Content-Type: {} | User-Agent: {}",
                transactionId, method, fullUrl, clientIp, contentType, userAgent);

        if (shouldLogRequestBody(method) && request instanceof ContentCachingRequestWrapper) {
            logRequestBody((ContentCachingRequestWrapper) request, transactionId);
        }

        if (logger.isDebugEnabled()) {
            logRequestHeaders(request, transactionId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        try {
            String transactionId = (String) request.getAttribute(TRANSACTION_ID_KEY);
            Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);

            if (transactionId == null || startTime == null) {
                return;
            }

            logRequestCompletion(request, response, ex, transactionId, startTime);

            if (response instanceof ContentCachingResponseWrapper && !isSensitiveEndpoint(request.getRequestURI())) {
                logResponseBody((ContentCachingResponseWrapper) response, transactionId);
            } else if (isSensitiveEndpoint(request.getRequestURI())) {
                logger.info("RESPONSE BODY - TransactionId: {} | [HIDDEN - SENSITIVE ENDPOINT]", transactionId);
            }
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

    private boolean logRequestBody(ContentCachingRequestWrapper request, String transactionId) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return false;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        String maskedBody = maskSensitiveData(body);
        String truncatedBody = truncateIfNeeded(maskedBody);
        logger.info("REQUEST BODY - TransactionId: {} | Body: {}", transactionId, truncatedBody);

        return true;
    }

    private boolean logResponseBody(ContentCachingResponseWrapper response, String transactionId) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return false;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        String truncatedBody = truncateIfNeeded(body);
        logger.info("RESPONSE BODY - TransactionId: {} | Body: {}", transactionId, truncatedBody);

        try {
            response.copyBodyToResponse();
        } catch (Exception e) {
            logger.error("Error copying response body", e);
        }

        return true;
    }

    private boolean isSensitiveEndpoint(String uri) {
        return SENSITIVE_ENDPOINTS.stream().anyMatch(uri::contains);
    }

    private String maskSensitiveData(String body) {
        return body.replaceAll("(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1****$3")
                .replaceAll("(\"newPassword\"\\s*:\\s*\")([^\"]*)(\")", "$1****$3")
                .replaceAll("(\"oldPassword\"\\s*:\\s*\")([^\"]*)(\")", "$1****$3");
    }

    private boolean shouldLogRequestBody(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method);
    }

    private String truncateIfNeeded(String content) {
        if (content.length() > MAX_PAYLOAD_LENGTH) {
            return content.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
        }

        return content;
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

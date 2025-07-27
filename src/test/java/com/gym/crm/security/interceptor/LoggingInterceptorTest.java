package com.gym.crm.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingInterceptorTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ContentCachingRequestWrapper requestWrapper;
    @Mock
    private ContentCachingResponseWrapper responseWrapper;
    @Mock
    private Object handler;
    @InjectMocks
    private LoggingInterceptor interceptor;

    @Test
    void preHandle_shouldSetTransactionIdAndLogRequest() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getQueryString()).thenReturn("param=value");
        when(request.getHeader("User-Agent")).thenReturn("JUnit Test Agent");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of()));

        boolean actual = interceptor.preHandle(request, response, handler);

        assertThat(actual).isTrue();
        verify(request).setAttribute(eq("transactionId"), any(String.class));
        verify(request).setAttribute(eq("requestStartTime"), any(Long.class));
    }

    @Test
    void preHandle_shouldLogRequestBodyForPostRequest() throws Exception {
        String requestBody = "{\"name\":\"test\",\"password\":\"secret123\"}";

        when(requestWrapper.getMethod()).thenReturn("POST");
        when(requestWrapper.getRequestURI()).thenReturn("/api/v1/test");
        when(requestWrapper.getQueryString()).thenReturn(null);
        when(requestWrapper.getHeader("User-Agent")).thenReturn("JUnit");
        when(requestWrapper.getHeader("X-Forwarded-For")).thenReturn(null);
        when(requestWrapper.getHeader("X-Real-IP")).thenReturn(null);
        when(requestWrapper.getRemoteAddr()).thenReturn("127.0.0.1");
        when(requestWrapper.getContentType()).thenReturn("application/json");
        when(requestWrapper.getHeaderNames()).thenReturn(Collections.enumeration(List.of()));
        when(requestWrapper.getContentAsByteArray()).thenReturn(requestBody.getBytes(StandardCharsets.UTF_8));

        boolean actual = interceptor.preHandle(requestWrapper, response, handler);

        assertThat(actual).isTrue();
        verify(requestWrapper).getContentAsByteArray();
    }

    @Test
    void preHandle_shouldNotLogRequestBodyForGetRequest() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContentType()).thenReturn("application/json");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of()));

        boolean actual = interceptor.preHandle(request, response, handler);

        assertThat(actual).isTrue();
    }

    @Test
    void afterCompletion_shouldLogErrorRequest() {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 150;
        Exception exception = new RuntimeException("Test error");

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(response.getStatus()).thenReturn(500);

        interceptor.afterCompletion(request, response, handler, exception);

        verify(response).getStatus();
    }

    @Test
    void afterCompletion_shouldLogClientError() {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 75;

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(response.getStatus()).thenReturn(400);

        interceptor.afterCompletion(request, response, handler, null);

        verify(response).getStatus();
    }

    @Test
    void afterCompletion_shouldSkipLoggingIfAttributesMissing() {
        when(request.getAttribute("transactionId")).thenReturn(null);
        when(request.getAttribute("requestStartTime")).thenReturn(null);

        interceptor.afterCompletion(request, response, handler, null);

        verifyNoInteractions(response);
    }

    @Test
    void afterCompletion_shouldLogResponseBodyForNormalEndpoint() throws Exception {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 100;
        String responseBody = "{\"result\":\"success\"}";

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(responseWrapper.getStatus()).thenReturn(200);
        when(responseWrapper.getContentAsByteArray()).thenReturn(responseBody.getBytes(StandardCharsets.UTF_8));

        interceptor.afterCompletion(request, responseWrapper, handler, null);

        verify(responseWrapper).getContentAsByteArray();
        verify(responseWrapper).copyBodyToResponse();
    }

    @Test
    void afterCompletion_shouldHideResponseBodyForSensitiveEndpoint() {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 100;

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/register");
        when(responseWrapper.getStatus()).thenReturn(201);

        interceptor.afterCompletion(request, responseWrapper, handler, null);

        verify(responseWrapper).getStatus();
    }

    @Test
    void afterCompletion_shouldLogEmptyResponseBody() {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 100;

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/v1/users/1");
        when(responseWrapper.getStatus()).thenReturn(204);
        when(responseWrapper.getContentAsByteArray()).thenReturn(new byte[0]);

        interceptor.afterCompletion(request, responseWrapper, handler, null);

        verify(responseWrapper).getContentAsByteArray();
    }
}


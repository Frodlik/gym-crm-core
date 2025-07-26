package com.gym.crm.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private Object handler;
    @InjectMocks
    private LoggingInterceptor interceptor;

    @Test
    void preHandle_shouldSetTransactionIdAndLogRequest() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getQueryString()).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of()));

        boolean actual = interceptor.preHandle(request, response, handler);

        assertThat(actual).isTrue();
        verify(request).setAttribute(eq("transactionId"), any(String.class));
        verify(request).setAttribute(eq("requestStartTime"), any(Long.class));
    }

    @Test
    void afterCompletion_shouldLogSuccessRequest() throws Exception {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 100;

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, handler, null);

        verify(response).getStatus();
    }

    @Test
    void afterCompletion_shouldSkipLoggingIfAttributesMissing() throws Exception {
        when(request.getAttribute("transactionId")).thenReturn(null);
        when(request.getAttribute("requestStartTime")).thenReturn(null);

        interceptor.afterCompletion(request, response, handler, null);

        verifyNoInteractions(response);
    }

    @Test
    void afterCompletion_shouldLogExceptionIfThrown() throws Exception {
        String transactionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis() - 150;

        Exception exception = new RuntimeException("Test error");

        when(request.getAttribute("transactionId")).thenReturn(transactionId);
        when(request.getAttribute("requestStartTime")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(500);

        interceptor.afterCompletion(request, response, handler, exception);

        verify(response).getStatus();
    }
}


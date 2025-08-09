package com.gym.crm.util;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TokenExtractorTest {
    private static final String JWT_TOKEN = "jwt.access.token";
    private static final String REFRESH_TOKEN = "jwt.refresh.token";
    private static final String JWT_COOKIE_NAME = "access-token";
    private static final String REFRESH_COOKIE_NAME = "refresh-token";

    @InjectMocks
    private TokenExtractor tokenExtractor;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testExtractAccessToken_whenBearerTokenPresent_shouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + JWT_TOKEN);
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessToken(request);

        assertTrue(actual.isPresent());
        assertEquals(JWT_TOKEN, actual.get());
    }

    @Test
    void testExtractAccessToken_whenTokenInCookie_shouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(JWT_COOKIE_NAME, JWT_TOKEN));
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessToken(request);

        assertTrue(actual.isPresent());
        assertEquals(JWT_TOKEN, actual.get());
    }

    @Test
    void testExtractAccessToken_whenBearerTokenAndCookie_shouldPreferBearerToken() {
        String bearerToken = "bearer.token";
        String cookieToken = "cookie.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + bearerToken);
        request.setCookies(new Cookie(JWT_COOKIE_NAME, cookieToken));
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessToken(request);

        assertTrue(actual.isPresent());
        assertEquals(bearerToken, actual.get());
    }

    @Test
    void testExtractAccessToken_whenNoToken_shouldReturnEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessToken(request);

        assertTrue(actual.isEmpty());
    }

    @Test
    void testExtractAccessToken_whenInvalidBearerHeader_shouldReturnEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "InvalidPrefix " + JWT_TOKEN);
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessToken(request);

        assertTrue(actual.isEmpty());
    }

    @Test
    void testExtractRefreshToken_whenRefreshTokenInCookie_shouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(REFRESH_COOKIE_NAME, REFRESH_TOKEN));
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractRefreshToken(request);

        assertTrue(actual.isPresent());
        assertEquals(REFRESH_TOKEN, actual.get());
    }

    @Test
    void testExtractRefreshToken_whenNoCookie_shouldReturnEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractRefreshToken(request);

        assertTrue(actual.isEmpty());
    }

    @Test
    void testExtractRefreshToken_whenWrongCookieName_shouldReturnEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("wrong-cookie-name", REFRESH_TOKEN));
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractRefreshToken(request);

        assertTrue(actual.isEmpty());
    }

    @Test
    void testExtractAccessTokenFromCurrentRequest_whenRequestInContext_shouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + JWT_TOKEN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessTokenFromCurrentRequest();

        assertTrue(actual.isPresent());
        assertEquals(JWT_TOKEN, actual.get());
    }

    @Test
    void testExtractAccessTokenFromCurrentRequest_whenNoRequestContext_shouldReturnEmpty() {
        RequestContextHolder.resetRequestAttributes();
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessTokenFromCurrentRequest();

        assertTrue(actual.isEmpty());
    }

    @Test
    void testExtractRefreshTokenFromCurrentRequest_whenRequestInContext_shouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(REFRESH_COOKIE_NAME, REFRESH_TOKEN));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractRefreshTokenFromCurrentRequest();

        assertTrue(actual.isPresent());
        assertEquals(REFRESH_TOKEN, actual.get());
    }

    @Test
    void testExtractTokenFromCookies_whenMultipleCookies_shouldReturnCorrectToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie[] cookies = {
                new Cookie("other-cookie", "other-value"),
                new Cookie(JWT_COOKIE_NAME, JWT_TOKEN),
                new Cookie("another-cookie", "another-value")
        };
        request.setCookies(cookies);
        setTokenProperties();

        Optional<String> actual = tokenExtractor.extractAccessToken(request);

        assertTrue(actual.isPresent());
        assertEquals(JWT_TOKEN, actual.get());
    }

    private void setTokenProperties() {
        ReflectionTestUtils.setField(tokenExtractor, "jwtCookieName", JWT_COOKIE_NAME);
        ReflectionTestUtils.setField(tokenExtractor, "refreshCookieName", REFRESH_COOKIE_NAME);
    }
}
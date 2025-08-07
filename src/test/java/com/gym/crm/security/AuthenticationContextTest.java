package com.gym.crm.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationContextTest {
    private static final String USERNAME = "naga.siren";
    private static final String JWT_TOKEN = "jwt.token.string";
    private static final String JWT_COOKIE_NAME = "auth-token";

    @Mock
    private CustomUserDetailsService userDetailsService;
    @InjectMocks
    private AuthenticationContext authenticationContext;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testGetCurrentUsername_whenUserAuthenticated_shouldReturnUsername() {
        CustomUserDetails userDetails = new CustomUserDetails(USERNAME, "password", true, "TRAINEE");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        Optional<String> result = authenticationContext.getCurrentUsername();

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get());
    }

    @Test
    void testGetCurrentUsername_whenUserNotAuthenticated_shouldReturnNull() {
        SecurityContextHolder.clearContext();

        Optional<String> result = authenticationContext.getCurrentUsername();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCurrentUserType_whenUserAuthenticated_shouldReturnUserType() {
        CustomUserDetails userDetails = new CustomUserDetails(USERNAME, "password", true, "TRAINEE");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isPresent());
        assertEquals("TRAINEE", result.get());
    }

    @Test
    void testGetCurrentUserType_whenUserNotAuthenticated_shouldReturnNull() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractTokenFromRequest_whenTokenInAuthorizationHeader_shouldReturnToken() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer " + JWT_TOKEN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        Optional<String> result = authenticationContext.extractTokenFromRequest();

        assertTrue(result.isPresent());
        assertEquals(JWT_TOKEN, result.get());
    }

    @Test
    void testExtractTokenFromRequest_whenTokenInCookie_shouldReturnToken() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setCookies(new Cookie(JWT_COOKIE_NAME, JWT_TOKEN));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
        setJwtCookieName();

        Optional<String> result = authenticationContext.extractTokenFromRequest();

        assertTrue(result.isPresent());
        assertEquals(JWT_TOKEN, result.get());
    }

    @Test
    void testExtractTokenFromRequest_whenNoToken_shouldReturnEmpty() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        Optional<String> result = authenticationContext.extractTokenFromRequest();

        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractTokenFromRequest_whenNoRequestContext_shouldReturnEmpty() {
        RequestContextHolder.resetRequestAttributes();

        Optional<String> result = authenticationContext.extractTokenFromRequest();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCurrentUserType_whenUserTypeInRequestAttribute_shouldReturnUserType() {
        SecurityContextHolder.clearContext();
        String expectedUserType = "TRAINER";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setAttribute("userType", expectedUserType);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isPresent());
        assertEquals(expectedUserType, result.get());
    }

    @Test
    void testGetCurrentUserType_whenUserTypeInSession_shouldReturnUserType() {
        SecurityContextHolder.clearContext();
        String expectedUserType = "TRAINEE";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("userType", expectedUserType);
        mockRequest.setSession(mockSession);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isPresent());
        assertEquals(expectedUserType, result.get());
    }

    private void setJwtCookieName() {
        ReflectionTestUtils.setField(authenticationContext, "jwtCookieName", JWT_COOKIE_NAME);
    }
}

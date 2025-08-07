package com.gym.crm.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationContextTest {
    private static final String USERNAME = "john.doe";
    private static final String JWT_TOKEN = "jwt.token.string";
    private static final String JWT_COOKIE_NAME = "auth-token";

    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
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

        String result = authenticationContext.getCurrentUsername();

        assertEquals(USERNAME, result);
    }

    @Test
    void testGetCurrentUsername_whenUserNotAuthenticated_shouldReturnNull() {
        SecurityContextHolder.clearContext();

        String result = authenticationContext.getCurrentUsername();

        assertNull(result);
    }

    @Test
    void testGetCurrentUserType_whenUserAuthenticated_shouldReturnUserType() {
        CustomUserDetails userDetails = new CustomUserDetails(USERNAME, "password", true, "TRAINEE");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        String result = authenticationContext.getCurrentUserType();

        assertEquals("TRAINEE", result);
    }

    @Test
    void testGetCurrentUserType_whenUserNotAuthenticated_shouldReturnNull() {
        SecurityContextHolder.clearContext();

        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            String result = authenticationContext.getCurrentUserType();

            assertNull(result);
        }
    }

    @Test
    void testExtractTokenFromRequest_whenTokenInAuthorizationHeader_shouldReturnToken() {
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);

        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);

            String result = authenticationContext.extractTokenFromRequest();

            assertEquals(JWT_TOKEN, result);
        }
    }

    @Test
    void testExtractTokenFromRequest_whenTokenInCookie_shouldReturnToken() {
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, JWT_TOKEN);
        Cookie[] cookies = {jwtCookie};
        setJwtCookieName();

        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getCookies()).thenReturn(cookies);

            String result = authenticationContext.extractTokenFromRequest();

            assertEquals(JWT_TOKEN, result);
        }
    }

    @Test
    void testExtractTokenFromRequest_whenNoToken_shouldReturnNull() {
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);

        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getCookies()).thenReturn(null);

            String result = authenticationContext.extractTokenFromRequest();

            assertNull(result);
        }
    }

    @Test
    void testExtractTokenFromRequest_whenNoRequestContext_shouldReturnNull() {
        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            String result = authenticationContext.extractTokenFromRequest();

            assertNull(result);
        }
    }

    @Test
    void testGetCurrentUserType_whenUserTypeInRequestAttribute_shouldReturnUserType() {
        SecurityContextHolder.clearContext();
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        String expectedUserType = "TRAINER";

        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getAttribute("userType")).thenReturn(expectedUserType);

            String result = authenticationContext.getCurrentUserType();

            assertEquals(expectedUserType, result);
        }
    }

    @Test
    void testGetCurrentUserType_whenUserTypeInSession_shouldReturnUserType() {
        SecurityContextHolder.clearContext();
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        String expectedUserType = "TRAINEE";

        try (MockedStatic<RequestContextHolder> mockedStatic = Mockito.mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getAttribute("userType")).thenReturn(null);
            when(request.getSession(false)).thenReturn(session);
            when(session.getAttribute("userType")).thenReturn(expectedUserType);

            String result = authenticationContext.getCurrentUserType();

            assertEquals(expectedUserType, result);
        }
    }

    private void setJwtCookieName() {
        ReflectionTestUtils.setField(authenticationContext, "jwtCookieName", JWT_COOKIE_NAME);
    }
}

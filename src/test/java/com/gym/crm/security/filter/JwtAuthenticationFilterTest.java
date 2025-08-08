package com.gym.crm.security.filter;

import com.gym.crm.security.CustomUserDetails;
import com.gym.crm.security.CustomUserDetailsService;
import com.gym.crm.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String USERNAME = "shadow.fiend";
    private static final String JWT_TOKEN = "jwt.token.string";
    private static final String JWT_COOKIE_NAME = "auth-token";
    private static final String REQUEST_URI = "/api/trainees/profile";

    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_whenValidTokenInAuthorizationHeader_shouldSetAuthentication() throws ServletException, IOException {
        CustomUserDetails userDetails = buildCustomUserDetails();
        setJwtCookieName();

        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtTokenUtil.getUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenUtil.validateAccessToken(JWT_TOKEN, USERNAME)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenUtil, times(2)).getUsernameFromToken(JWT_TOKEN);
        verify(jwtTokenUtil).validateAccessToken(JWT_TOKEN, USERNAME);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenValidTokenInCookie_shouldSetAuthentication() throws ServletException, IOException {
        CustomUserDetails userDetails = buildCustomUserDetails();
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, JWT_TOKEN);
        Cookie[] cookies = {jwtCookie};
        setJwtCookieName();

        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenUtil.getUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenUtil.validateAccessToken(JWT_TOKEN, USERNAME)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenUtil, times(2)).getUsernameFromToken(JWT_TOKEN);
        verify(jwtTokenUtil).validateAccessToken(JWT_TOKEN, USERNAME);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenNoToken_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenUtil, never()).getUsernameFromToken(JWT_TOKEN);
        verify(jwtTokenUtil, never()).validateToken(JWT_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenInvalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        setJwtCookieName();

        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtTokenUtil.getUsernameFromToken(JWT_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenUtil).getUsernameFromToken(JWT_TOKEN);
        verify(jwtTokenUtil, never()).validateToken(JWT_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenTokenValidationFails_shouldNotSetAuthentication() throws ServletException, IOException {
        setJwtCookieName();

        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtTokenUtil.getUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenUtil.validateAccessToken(JWT_TOKEN, USERNAME)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenUtil).getUsernameFromToken(JWT_TOKEN);
        verify(jwtTokenUtil).validateAccessToken(JWT_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenUserAlreadyAuthenticated_shouldSkipAuthentication() throws ServletException, IOException {
        CustomUserDetails userDetails = buildCustomUserDetails();
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        setJwtCookieName();

        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);
        when(jwtTokenUtil.getUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenUtil.validateAccessToken(JWT_TOKEN, USERNAME)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenUtil).getUsernameFromToken(JWT_TOKEN);
        verify(jwtTokenUtil).validateAccessToken(JWT_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
    }

    private CustomUserDetails buildCustomUserDetails() {
        return new CustomUserDetails(USERNAME, "password", true, "TRAINEE");
    }

    private void setJwtCookieName() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "jwtCookieName", JWT_COOKIE_NAME);
    }
}

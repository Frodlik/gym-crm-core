package com.gym.crm.security.filter;

import com.gym.crm.security.CustomUserDetails;
import com.gym.crm.security.service.CustomUserDetailsService;
import com.gym.crm.security.JwtTokenHandler;
import com.gym.crm.util.TokenExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String USERNAME = "shadow.fiend";
    private static final String ACCESS_TOKEN = "access.token.string";

    @Mock
    private JwtTokenHandler jwtTokenHandler;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private TokenExtractor tokenExtractor;
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
    void testDoFilterInternal_whenValidTokenProvided_shouldSetAuthentication() throws ServletException, IOException {
        CustomUserDetails userDetails = createCustomUserDetails();

        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtTokenHandler.getUsernameFromToken(ACCESS_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenHandler.validateAccessToken(ACCESS_TOKEN, USERNAME)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenExtractor).extractAccessToken(request);
        verify(jwtTokenHandler, times(2)).getUsernameFromToken(ACCESS_TOKEN);
        verify(jwtTokenHandler).validateAccessToken(ACCESS_TOKEN, USERNAME);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenNoTokenProvided_shouldNotSetAuthentication() throws ServletException, IOException {
        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenExtractor).extractAccessToken(request);
        verify(jwtTokenHandler, never()).getUsernameFromToken(ACCESS_TOKEN);
        verify(jwtTokenHandler, never()).validateAccessToken(ACCESS_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenInvalidTokenProvided_shouldNotSetAuthentication() throws ServletException, IOException {
        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtTokenHandler.getUsernameFromToken(ACCESS_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenExtractor).extractAccessToken(request);
        verify(jwtTokenHandler).getUsernameFromToken(ACCESS_TOKEN);
        verify(jwtTokenHandler, never()).validateAccessToken(ACCESS_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenTokenValidationFails_shouldNotSetAuthentication() throws ServletException, IOException {
        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtTokenHandler.getUsernameFromToken(ACCESS_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenHandler.validateAccessToken(ACCESS_TOKEN, USERNAME)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenExtractor).extractAccessToken(request);
        verify(jwtTokenHandler).getUsernameFromToken(ACCESS_TOKEN);
        verify(jwtTokenHandler).validateAccessToken(ACCESS_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_whenUserAlreadyAuthenticated_shouldSkipAuthentication() throws ServletException, IOException {
        CustomUserDetails userDetails = createCustomUserDetails();
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtTokenHandler.getUsernameFromToken(ACCESS_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenHandler.validateAccessToken(ACCESS_TOKEN, USERNAME)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenExtractor).extractAccessToken(request);
        verify(jwtTokenHandler).getUsernameFromToken(ACCESS_TOKEN);
        verify(jwtTokenHandler).validateAccessToken(ACCESS_TOKEN, USERNAME);
        verify(userDetailsService, never()).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_whenUserDetailsServiceFails_shouldNotSetAuthentication() throws ServletException, IOException {
        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(ACCESS_TOKEN));
        when(jwtTokenHandler.getUsernameFromToken(ACCESS_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenHandler.validateAccessToken(ACCESS_TOKEN, USERNAME)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenThrow(new RuntimeException("User not found"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenExtractor).extractAccessToken(request);
        verify(jwtTokenHandler, times(2)).getUsernameFromToken(ACCESS_TOKEN);
        verify(jwtTokenHandler).validateAccessToken(ACCESS_TOKEN, USERNAME);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private CustomUserDetails createCustomUserDetails() {
        return new CustomUserDetails(USERNAME, "password", true, "TRAINEE");
    }
}

package com.gym.crm.security;

import com.gym.crm.security.service.CustomUserDetailsService;
import com.gym.crm.util.TokenExtractor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationContextTest {
    private static final String USERNAME = "naga.siren";
    private static final String ACCESS_TOKEN = "access.token.string";
    private static final String TRAINEE_TYPE = "TRAINEE";
    private static final String TRAINER_TYPE = "TRAINER";

    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private TokenExtractor tokenExtractor;
    @InjectMocks
    private AuthenticationContext authenticationContext;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUsername_whenUserAuthenticated_shouldReturnUsername() {
        CustomUserDetails userDetails = createUserDetails(TRAINEE_TYPE);
        setAuthenticatedUser(userDetails);

        Optional<String> result = authenticationContext.getCurrentUsername();

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get());
    }

    @Test
    void testGetCurrentUsername_whenUserNotAuthenticated_shouldReturnEmpty() {
        SecurityContextHolder.clearContext();

        Optional<String> result = authenticationContext.getCurrentUsername();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCurrentUserType_whenUserAuthenticated_shouldReturnUserType() {
        CustomUserDetails userDetails = createUserDetails(TRAINEE_TYPE);
        setAuthenticatedUser(userDetails);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isPresent());
        assertEquals(TRAINEE_TYPE, result.get());
    }

    @Test
    void testGetCurrentUserType_whenUserNotAuthenticated_shouldReturnEmpty() {
        SecurityContextHolder.clearContext();

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCurrentUserType_whenUserDetailsServiceFails_shouldReturnEmpty() {
        CustomUserDetails userDetails = createUserDetails(TRAINEE_TYPE);
        setAuthenticatedUser(userDetails);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenThrow(new RuntimeException("User not found"));

        Optional<String> result = authenticationContext.getCurrentUserType();

        assertTrue(result.isEmpty());
    }

    @Test
    void testIsCurrentUserTrainee_whenUserIsTrainee_shouldReturnTrue() {
        CustomUserDetails userDetails = createUserDetails(TRAINEE_TYPE);
        setAuthenticatedUser(userDetails);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        boolean result = authenticationContext.isCurrentUserTrainee();

        assertTrue(result);
    }

    @Test
    void testIsCurrentUserTrainee_whenUserIsTrainer_shouldReturnFalse() {
        CustomUserDetails userDetails = createUserDetails(TRAINER_TYPE);
        setAuthenticatedUser(userDetails);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        boolean result = authenticationContext.isCurrentUserTrainee();

        assertFalse(result);
    }

    @Test
    void testIsCurrentUserTrainer_whenUserIsTrainer_shouldReturnTrue() {
        CustomUserDetails userDetails = createUserDetails(TRAINER_TYPE);
        setAuthenticatedUser(userDetails);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        boolean result = authenticationContext.isCurrentUserTrainer();

        assertTrue(result);
    }

    @Test
    void testIsCurrentUserTrainer_whenUserIsTrainee_shouldReturnFalse() {
        CustomUserDetails userDetails = createUserDetails(TRAINEE_TYPE);
        setAuthenticatedUser(userDetails);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        boolean result = authenticationContext.isCurrentUserTrainer();

        assertFalse(result);
    }

    @Test
    void testExtractTokenFromRequest_whenTokenAvailable_shouldReturnToken() {
        when(tokenExtractor.extractAccessTokenFromCurrentRequest()).thenReturn(Optional.of(ACCESS_TOKEN));

        Optional<String> result = authenticationContext.extractTokenFromRequest();

        assertTrue(result.isPresent());
        assertEquals(ACCESS_TOKEN, result.get());
    }

    @Test
    void testExtractTokenFromRequest_whenNoToken_shouldReturnEmpty() {
        when(tokenExtractor.extractAccessTokenFromCurrentRequest()).thenReturn(Optional.empty());

        Optional<String> result = authenticationContext.extractTokenFromRequest();

        assertTrue(result.isEmpty());
    }

    private CustomUserDetails createUserDetails(String userType) {
        return new CustomUserDetails(USERNAME, "password", true, userType);
    }

    private void setAuthenticatedUser(CustomUserDetails userDetails) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
    }
}

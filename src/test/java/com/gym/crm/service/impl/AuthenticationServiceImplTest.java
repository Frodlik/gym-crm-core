package com.gym.crm.service.impl;

import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.exception.NotAuthenticatedException;
import com.gym.crm.exception.UserBlockedException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.security.service.BruteForceProtectionService;
import com.gym.crm.service.enums.UserType;
import com.gym.crm.security.JwtTokenHandler;
import com.gym.crm.util.TokenExtractor;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    private static final String USERNAME = "phantom.assassin";
    private static final String PASSWORD = "ultraPassword123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String ACCESS_TOKEN = "access.token.string";
    private static final String REFRESH_TOKEN = "refresh.token.string";
    private static final String ACCESS_COOKIE_NAME = "access-token";
    private static final String REFRESH_COOKIE_NAME = "refresh-token";
    private static final Long JWT_EXPIRATION = 86400L;
    private static final Long REFRESH_EXPIRATION = 604800L;

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private JwtTokenHandler jwtTokenHandler;
    @Mock
    private TokenExtractor tokenExtractor;
    @Mock
    private BruteForceProtectionService bruteForceProtectionService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void testAuthenticateAndSetToken_whenValidTraineeCredentials_shouldSetJwtCookies() {
        Trainee trainee = createTrainee();
        setJwtProperties();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenHandler.generateAccessToken(USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtTokenHandler.generateRefreshToken(USERNAME)).thenReturn(REFRESH_TOKEN);

        service.authenticateAndSetToken(USERNAME, PASSWORD, response);

        verify(bruteForceProtectionService).recordSuccessfulAttempt(USERNAME);
        verify(response, times(2)).addCookie(cookieCaptor.capture());
        Cookie accessCookie = cookieCaptor.getAllValues().get(0);
        Cookie refreshCookie = cookieCaptor.getAllValues().get(1);
        assertEquals(ACCESS_COOKIE_NAME, accessCookie.getName());
        assertEquals(ACCESS_TOKEN, accessCookie.getValue());
        assertTrue(accessCookie.isHttpOnly());
        assertEquals(REFRESH_COOKIE_NAME, refreshCookie.getName());
        assertEquals(REFRESH_TOKEN, refreshCookie.getValue());
        assertTrue(refreshCookie.isHttpOnly());
    }

    @Test
    void testAuthenticateAndSetToken_whenUserIsBlocked_shouldThrowUserBlockedException() {
        LocalDateTime blockExpiration = LocalDateTime.now().plusMinutes(5);

        when(bruteForceProtectionService.isUserBlocked(USERNAME)).thenReturn(true);
        when(bruteForceProtectionService.getBlockExpiration(USERNAME)).thenReturn(blockExpiration);

        UserBlockedException exception = assertThrows(UserBlockedException.class,
                () -> service.authenticateAndSetToken(USERNAME, PASSWORD, response));

        assertEquals(USERNAME, exception.getUsername());
        assertEquals(blockExpiration, exception.getBlockedUntil());
        verify(bruteForceProtectionService, never()).recordFailedAttempt(any());
        verify(bruteForceProtectionService, never()).recordSuccessfulAttempt(any());
        verify(response, never()).addCookie(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenValidTrainerCredentials_shouldSetJwtCookies() {
        Trainer trainer = createTrainer();
        setJwtProperties();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenHandler.generateAccessToken(USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtTokenHandler.generateRefreshToken(USERNAME)).thenReturn(REFRESH_TOKEN);

        service.authenticateAndSetToken(USERNAME, PASSWORD, response);

        verify(response, times(2)).addCookie(cookieCaptor.capture());
        Cookie accessCookie = cookieCaptor.getAllValues().getFirst();
        assertEquals(ACCESS_COOKIE_NAME, accessCookie.getName());
        assertEquals(ACCESS_TOKEN, accessCookie.getValue());
    }

    @Test
    void testAuthenticateAndSetToken_whenInvalidPassword_shouldThrowNotAuthenticatedException() {
        Trainee trainee = createTrainee();

        when(bruteForceProtectionService.isUserBlocked(USERNAME)).thenReturn(false);
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class,
                () -> service.authenticateAndSetToken(USERNAME, PASSWORD, response));

        assertEquals("Invalid password for trainee: " + USERNAME, exception.getMessage());
        verify(bruteForceProtectionService).recordFailedAttempt(USERNAME);
        verify(response, never()).addCookie(any());
        verify(jwtTokenHandler, never()).generateAccessToken(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenUserNotFound_shouldThrowCoreServiceException() {
        when(bruteForceProtectionService.isUserBlocked(USERNAME)).thenReturn(false);
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.empty());

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class,
                () -> service.authenticateAndSetToken(USERNAME, PASSWORD, response));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(bruteForceProtectionService).recordFailedAttempt(USERNAME);
        verify(response, never()).addCookie(any());
        verify(jwtTokenHandler, never()).generateAccessToken(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenUsernameIsNull_shouldThrowCoreServiceException() {
        String nullUsername = null;

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.authenticateAndSetToken(nullUsername, PASSWORD, response));

        assertEquals("Username and password are required", exception.getMessage());
        verify(traineeRepository, never()).findTraineeByUser_Username(any());
        verify(trainerRepository, never()).findTrainerByUser_Username(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenPasswordIsNull_shouldThrowCoreServiceException() {
        String nullPassword = null;

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.authenticateAndSetToken(USERNAME, nullPassword, response));

        assertEquals("Username and password are required", exception.getMessage());
        verify(traineeRepository, never()).findTraineeByUser_Username(any());
        verify(trainerRepository, never()).findTrainerByUser_Username(any());
    }

    @Test
    void testRefreshAccessToken_whenValidRefreshToken_shouldGenerateNewTokens() {
        setJwtProperties();

        when(tokenExtractor.extractRefreshToken(request)).thenReturn(Optional.of(REFRESH_TOKEN));
        when(jwtTokenHandler.getUsernameFromToken(REFRESH_TOKEN)).thenReturn(USERNAME);
        when(jwtTokenHandler.validateRefreshToken(REFRESH_TOKEN, USERNAME)).thenReturn(true);
        when(jwtTokenHandler.generateAccessToken(USERNAME)).thenReturn("new.access.token");
        when(jwtTokenHandler.generateRefreshToken(USERNAME)).thenReturn("new.refresh.token");

        service.refreshAccessToken(request, response);

        verify(tokenExtractor).extractRefreshToken(request);
        verify(jwtTokenHandler).validateRefreshToken(REFRESH_TOKEN, USERNAME);
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void testRefreshAccessToken_whenNoRefreshToken_shouldThrowNotAuthenticatedException() {
        when(tokenExtractor.extractRefreshToken(request)).thenReturn(Optional.empty());

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class,
                () -> service.refreshAccessToken(request, response));

        assertEquals("Refresh token not found", exception.getMessage());
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testValidateCredentials_whenValidTraineeCredentials_shouldReturnTraineeType() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        UserType actual = service.validateCredentials(USERNAME, PASSWORD);

        assertEquals("TRAINEE", actual.toString());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
    }

    @Test
    void testValidateCredentials_whenValidTrainerCredentials_shouldReturnTrainerType() {
        Trainer trainer = createTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        UserType actual = service.validateCredentials(USERNAME, PASSWORD);

        assertEquals("TRAINER", actual.toString());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(trainerRepository).findTrainerByUser_Username(USERNAME);
    }

    @Test
    void testValidateTraineeCredentials_whenUserIsTrainee_shouldNotThrowException() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        service.validateTraineeCredentials(USERNAME, PASSWORD);

        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
    }

    @Test
    void testValidateTraineeCredentials_whenUserIsTrainer_shouldThrowNotAuthenticatedException() {
        Trainer trainer = createTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class,
                () -> service.validateTraineeCredentials(USERNAME, PASSWORD));

        assertEquals("Access denied. Only trainees can perform this operation", exception.getMessage());
    }

    @Test
    void testValidateTrainerCredentials_whenUserIsTrainer_shouldNotThrowException() {
        Trainer trainer = createTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        service.validateTrainerCredentials(USERNAME, PASSWORD);

        verify(trainerRepository).findTrainerByUser_Username(USERNAME);
    }

    @Test
    void testValidateTrainerCredentials_whenUserIsTrainee_shouldThrowNotAuthenticatedException() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class,
                () -> service.validateTrainerCredentials(USERNAME, PASSWORD));

        assertEquals("Access denied. Only trainers can perform this operation", exception.getMessage());
    }

    @Test
    void testLogout_whenBothTokensPresent_shouldClearCookiesAndLogUsername() {
        String accessToken = "valid-access-token";
        String refreshToken = "valid-refresh-token";
        setJwtProperties();

        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(accessToken));
        when(tokenExtractor.extractRefreshToken(request)).thenReturn(Optional.of(refreshToken));
        when(jwtTokenHandler.getUsernameFromToken(accessToken)).thenReturn(USERNAME);

        service.logout(request, response);

        verify(response, times(2)).addCookie(cookieCaptor.capture());
        var cookies = cookieCaptor.getAllValues();
        verifyTokenCookieCleared(cookies, ACCESS_COOKIE_NAME);
        verifyTokenCookieCleared(cookies, REFRESH_COOKIE_NAME);
        verify(jwtTokenHandler).getUsernameFromToken(accessToken);
    }

    @Test
    void testLogout_whenOnlyAccessTokenPresent_shouldClearCookiesAndLogUsername() {
        String accessToken = "valid-access-token";
        setJwtProperties();

        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.of(accessToken));
        when(tokenExtractor.extractRefreshToken(request)).thenReturn(Optional.empty());
        when(jwtTokenHandler.getUsernameFromToken(accessToken)).thenReturn(USERNAME);

        service.logout(request, response);

        verify(response, times(2)).addCookie(any(Cookie.class));
        verify(jwtTokenHandler).getUsernameFromToken(accessToken);
    }

    @Test
    void testLogout_whenOnlyRefreshTokenPresent_shouldClearCookies() {
        String refreshToken = "valid-refresh-token";
        setJwtProperties();

        when(tokenExtractor.extractAccessToken(request)).thenReturn(Optional.empty());
        when(tokenExtractor.extractRefreshToken(request)).thenReturn(Optional.of(refreshToken));

        service.logout(request, response);

        verify(response, times(2)).addCookie(any(Cookie.class));
        verify(jwtTokenHandler, never()).getUsernameFromToken(any());
    }

    private Trainee createTrainee() {
        User user = createUser();
        return Trainee.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private Trainer createTrainer() {
        User user = createUser();
        return Trainer.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private User createUser() {
        return User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .firstName("Phantom")
                .lastName("Assassin")
                .isActive(true)
                .build();
    }

    private void setJwtProperties() {
        ReflectionTestUtils.setField(service, "jwtCookieSecure", false);
        ReflectionTestUtils.setField(service, "jwtCookieHttpOnly", true);
        ReflectionTestUtils.setField(service, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(service, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private void verifyTokenCookieCleared(java.util.List<Cookie> cookies, String cookieName) {
        Cookie cookie = cookies.stream()
                .filter(c -> cookieName.equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cookie " + cookieName + " not found"));

        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
    }
}

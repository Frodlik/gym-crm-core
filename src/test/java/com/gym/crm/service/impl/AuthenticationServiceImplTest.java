package com.gym.crm.service.impl;

import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.exception.NotAuthenticatedException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.util.JwtTokenUtil;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    private static final String USERNAME = "phantom.assassin";
    private static final String PASSWORD = "ultraPassword123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String JWT_TOKEN = "jwt.token.string";
    private static final String JWT_COOKIE_NAME = "auth-token";
    private static final Long JWT_EXPIRATION = 86400L;

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void testAuthenticateAndSetToken_whenValidTraineeCredentials_shouldSetJwtCookie() {
        Trainee trainee = buildTrainee();
        setJwtProperties();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenUtil.generateToken(USERNAME)).thenReturn(JWT_TOKEN);

        authenticationService.authenticateAndSetToken(USERNAME, PASSWORD, response);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals(JWT_COOKIE_NAME, capturedCookie.getName());
        assertEquals(JWT_TOKEN, capturedCookie.getValue());
        assertTrue(capturedCookie.isHttpOnly());
        assertEquals("/", capturedCookie.getPath());
        assertEquals(JWT_EXPIRATION.intValue(), capturedCookie.getMaxAge());
    }

    @Test
    void testAuthenticateAndSetToken_whenValidTrainerCredentials_shouldSetJwtCookie() {
        Trainer trainer = buildTrainer();
        setJwtProperties();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtTokenUtil.generateToken(USERNAME)).thenReturn(JWT_TOKEN);

        authenticationService.authenticateAndSetToken(USERNAME, PASSWORD, response);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals(JWT_COOKIE_NAME, capturedCookie.getName());
        assertEquals(JWT_TOKEN, capturedCookie.getValue());
    }

    @Test
    void testAuthenticateAndSetToken_whenInvalidCredentials_shouldThrowNotAuthenticatedException() {
        Trainee trainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class,
                () -> authenticationService.authenticateAndSetToken(USERNAME, PASSWORD, response));

        assertEquals("Invalid password for trainee: " + USERNAME, exception.getMessage());
        verify(response, never()).addCookie(any());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenUserNotFound_shouldThrowCoreServiceException() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> authenticationService.authenticateAndSetToken(USERNAME, PASSWORD, response));

        assertEquals("User not found: " + USERNAME, exception.getMessage());
        verify(response, never()).addCookie(any());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenUsernameIsNull_shouldThrowCoreServiceException() {
        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> authenticationService.authenticateAndSetToken(null, PASSWORD, response));

        assertEquals("Username and password are required", exception.getMessage());
        verify(traineeRepository, never()).findTraineeByUser_Username(any());
        verify(trainerRepository, never()).findTrainerByUser_Username(any());
    }

    @Test
    void testAuthenticateAndSetToken_whenPasswordIsNull_shouldThrowCoreServiceException() {
        String nullPassword = null;

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> authenticationService.authenticateAndSetToken(USERNAME, nullPassword, response));

        assertEquals("Username and password are required", exception.getMessage());
        verify(traineeRepository, never()).findTraineeByUser_Username(any());
        verify(trainerRepository, never()).findTrainerByUser_Username(any());
    }

    @Test
    void testValidateCredentials_whenValidTraineeCredentials_shouldReturnTraineeType() {
        Trainee trainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        String result = authenticationService.validateCredentials(USERNAME, PASSWORD);

        assertEquals("TRAINEE", result);
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
    }

    @Test
    void testValidateCredentials_whenValidTrainerCredentials_shouldReturnTrainerType() {
        Trainer trainer = buildTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        String result = authenticationService.validateCredentials(USERNAME, PASSWORD);

        assertEquals("TRAINER", result);
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(trainerRepository).findTrainerByUser_Username(USERNAME);
    }

    @Test
    void testValidateCredentials_whenUserNotFound_shouldThrowCoreServiceException() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> authenticationService.validateCredentials(USERNAME, PASSWORD));

        assertEquals("User not found: " + USERNAME, exception.getMessage());
    }

    @Test
    void testValidateTraineeCredentials_whenUserIsTrainee_shouldNotThrowException() {
        Trainee trainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        authenticationService.validateTraineeCredentials(USERNAME, PASSWORD);

        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
    }

    @Test
    void testValidateTraineeCredentials_whenUserIsTrainer_shouldThrowNotAuthenticatedException() {
        Trainer trainer = buildTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class, () -> authenticationService.validateTraineeCredentials(USERNAME, PASSWORD));

        assertEquals("Access denied. Only trainees can perform this operation.", exception.getMessage());
    }

    @Test
    void testValidateTrainerCredentials_whenUserIsTrainer_shouldNotThrowException() {
        Trainer trainer = buildTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        authenticationService.validateTrainerCredentials(USERNAME, PASSWORD);

        verify(trainerRepository).findTrainerByUser_Username(USERNAME);
    }

    @Test
    void testValidateTrainerCredentials_whenUserIsTrainee_shouldThrowNotAuthenticatedException() {
        Trainee trainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        NotAuthenticatedException exception = assertThrows(NotAuthenticatedException.class, () -> authenticationService.validateTrainerCredentials(USERNAME, PASSWORD));

        assertEquals("Access denied. Only trainers can perform this operation.", exception.getMessage());
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .firstName("Phantom")
                .lastName("Assassin")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .firstName("Phantom")
                .lastName("Lancer")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private void setJwtProperties() {
        ReflectionTestUtils.setField(authenticationService, "jwtCookieName", JWT_COOKIE_NAME);
        ReflectionTestUtils.setField(authenticationService, "jwtCookieSecure", false);
        ReflectionTestUtils.setField(authenticationService, "jwtCookieHttpOnly", true);
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", JWT_EXPIRATION);
    }
}

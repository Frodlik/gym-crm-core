package com.gym.crm.service.impl;

import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.exception.NotAuthenticatedException;
import com.gym.crm.exception.UserBlockedException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.security.service.BruteForceProtectionService;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.service.enums.UserType;
import com.gym.crm.security.JwtTokenHandler;
import com.gym.crm.util.TokenExtractor;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.gym.crm.service.enums.UserType.TRAINEE;
import static com.gym.crm.service.enums.UserType.TRAINER;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserCredentialsGenerator userCredentialsGenerator;
    private final JwtTokenHandler jwtTokenHandler;
    private final TokenExtractor tokenExtractor;
    private final BruteForceProtectionService bruteForceProtectionService;

    @Value("${jwt.cookie.secure}")
    private boolean jwtCookieSecure;

    @Value("${jwt.cookie.http-only}")
    private boolean jwtCookieHttpOnly;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    @Override
    public void authenticateAndSetToken(String username, String password, HttpServletResponse response) {
        validateInputCredentials(username, password);
        checkUserNotBlocked(username);

        try {
            UserType userType = performAuthentication(username, password);
            bruteForceProtectionService.recordSuccessfulAttempt(username);

            String accessToken = jwtTokenHandler.generateAccessToken(username);
            String refreshToken = jwtTokenHandler.generateRefreshToken(username);

            setTokenCookies(response, accessToken, refreshToken);
            logger.info("{} authenticated successfully: {}", userType, username);
        } catch (NotAuthenticatedException | CoreServiceException e) {
            bruteForceProtectionService.recordFailedAttempt(username);
            throw e;
        }
    }

    @Override
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = tokenExtractor.extractRefreshToken(request)
                .orElseThrow(() -> new NotAuthenticatedException("Refresh token not found"));

        try {
            String username = jwtTokenHandler.getUsernameFromToken(refreshToken);
            checkUserNotBlocked(username);

            boolean isValid = jwtTokenHandler.validateRefreshToken(refreshToken, username);
            if (!isValid) {
                throw new NotAuthenticatedException("Invalid refresh token");
            }

            String newAccessToken = jwtTokenHandler.generateAccessToken(username);
            String newRefreshToken = jwtTokenHandler.generateRefreshToken(username);

            setTokenCookies(response, newAccessToken, newRefreshToken);
            logger.debug("Tokens refreshed successfully for user: {}", username);
        } catch (Exception e) {
            throw new NotAuthenticatedException("Failed to refresh token");
        }
    }

    @Override
    public UserType validateCredentials(String username, String password) {
        validateInputCredentials(username, password);
        checkUserNotBlocked(username);

        try {
            UserType userType = performAuthentication(username, password);

            bruteForceProtectionService.recordSuccessfulAttempt(username);
            logger.debug("Credentials validated successfully for user: {}", username);

            return userType;
        } catch (NotAuthenticatedException | CoreServiceException e) {
            bruteForceProtectionService.recordFailedAttempt(username);
            throw e;
        }
    }

    @Override
    public void validateTraineeCredentials(String username, String password) {
        UserType userType = validateCredentials(username, password);

        if (userType != TRAINEE) {
            throw new NotAuthenticatedException("Access denied. Only trainees can perform this operation");
        }
    }

    @Override
    public void validateTrainerCredentials(String username, String password) {
        UserType userType = validateCredentials(username, password);

        if (userType != TRAINER) {
            throw new NotAuthenticatedException("Access denied. Only trainers can perform this operation");
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> accessTokenOpt = tokenExtractor.extractAccessToken(request);
        Optional<String> refreshTokenOpt = tokenExtractor.extractRefreshToken(request);

        if (accessTokenOpt.isEmpty() && refreshTokenOpt.isEmpty()) {
            return;
        }

        clearTokenCookies(response);

        accessTokenOpt.map(jwtTokenHandler::getUsernameFromToken)
                .ifPresent(username -> logger.info("User logged out successfully: {}", username));
    }

    private void validateInputCredentials(String username, String password) {
        if (username == null || password == null) {
            throw new CoreServiceException("Username and password are required");
        }
    }

    private void checkUserNotBlocked(String username) {
        if (bruteForceProtectionService.isUserBlocked(username)) {
            LocalDateTime blockExpiration = bruteForceProtectionService.getBlockExpiration(username);
            throw new UserBlockedException(username, blockExpiration);
        }
    }

    private UserType performAuthentication(String username, String password) {
        Optional<Trainee> traineeOpt = traineeRepository.findTraineeByUser_Username(username);
        if (traineeOpt.isPresent()) {
            validatePassword(password, traineeOpt.get().getUser().getPassword(), TRAINEE, username);

            return TRAINEE;
        }

        Optional<Trainer> trainerOpt = trainerRepository.findTrainerByUser_Username(username);
        if (trainerOpt.isPresent()) {
            validatePassword(password, trainerOpt.get().getUser().getPassword(), TRAINER, username);

            return TRAINER;
        }

        throw new NotAuthenticatedException("Invalid username or password");
    }

    private void validatePassword(String rawPassword, String encodedPassword, UserType userType, String username) {
        if (!userCredentialsGenerator.matches(rawPassword, encodedPassword)) {
            throw new NotAuthenticatedException(
                    String.format("Invalid password for %s: %s", userType.toString().toLowerCase(), username)
            );
        }
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAccessTokenCookie(response, accessToken);
        setRefreshTokenCookie(response, refreshToken);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("access-token", token);
        jwtCookie.setHttpOnly(jwtCookieHttpOnly);
        jwtCookie.setSecure(jwtCookieSecure);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtExpiration.intValue());

        response.addCookie(jwtCookie);
        logger.debug("Access token set in cookie");
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie refreshCookie = new Cookie("refresh-token", token);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(jwtCookieSecure);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshExpiration.intValue());

        response.addCookie(refreshCookie);
        logger.debug("Refresh token set in cookie");
    }

    private void clearTokenCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }

    private void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access-token", null);
        accessTokenCookie.setHttpOnly(jwtCookieHttpOnly);
        accessTokenCookie.setSecure(jwtCookieSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        logger.debug("Access token cookie cleared");
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refresh-token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(jwtCookieSecure);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(refreshTokenCookie);
        logger.debug("Refresh token cookie cleared");
    }
}

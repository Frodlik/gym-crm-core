package com.gym.crm.service.impl;

import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.exception.NotAuthenticatedException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.util.JwtTokenUtil;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final String TRAINER = "TRAINER";
    private static final String TRAINEE = "TRAINEE";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserCredentialsGenerator userCredentialsGenerator;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Value("${jwt.cookie.secure}")
    private boolean jwtCookieSecure;

    @Value("${jwt.cookie.http-only}")
    private boolean jwtCookieHttpOnly;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public void authenticateAndSetToken(String username, String password, HttpServletResponse response) {
        logger.info("Authenticating user: {}", username);

        if (username == null || password == null) {
            throw new CoreServiceException("Username and password are required");
        }

        String userType = validateUserCredentials(username, password);
        String token = jwtTokenUtil.generateToken(username);

        setJwtCookie(response, token);
        logger.info("{} authenticated successfully: {}", userType, username);
    }

    @Override
    public String validateCredentials(String username, String password) {
        logger.debug("Validating credentials for user: {}", username);

        if (username == null || password == null) {
            throw new NotAuthenticatedException("Username and password are required");
        }

        return validateUserCredentials(username, password);
    }

    @Override
    public void validateTraineeCredentials(String username, String password) {
        String userType = validateCredentials(username, password);
        if (!TRAINEE.equals(userType)) {
            throw new NotAuthenticatedException("Access denied. Only trainees can perform this operation.");
        }
    }

    @Override
    public void validateTrainerCredentials(String username, String password) {
        String userType = validateCredentials(username, password);
        if (!TRAINER.equals(userType)) {
            throw new NotAuthenticatedException("Access denied. Only trainers can perform this operation.");
        }
    }

    private String validateUserCredentials(String username, String password) {
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

        throw new CoreServiceException("User not found: " + username);
    }

    private void validatePassword(String rawPassword, String encodedPassword, String userType, String username) {
        if (!userCredentialsGenerator.matches(rawPassword, encodedPassword)) {
            throw new NotAuthenticatedException(
                    String.format("Invalid password for %s: %s", userType.toLowerCase(), username)
            );
        }
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie(jwtCookieName, token);
        jwtCookie.setHttpOnly(jwtCookieHttpOnly);
        jwtCookie.setSecure(jwtCookieSecure);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtExpiration.intValue());

        response.addCookie(jwtCookie);
        logger.debug("JWT token set in cookie: {}", jwtCookieName);
    }
}

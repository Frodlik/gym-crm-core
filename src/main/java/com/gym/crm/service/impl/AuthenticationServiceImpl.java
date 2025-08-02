package com.gym.crm.service.impl;

import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.exception.NotAuthenticatedException;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.util.UserCredentialsGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final String TRAINER = "TRAINER";
    private static final String TRAINEE = "TRAINEE";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserCredentialsGenerator userCredentialsGenerator;

    @Override
    public String validateCredentials(String username, String password) {
        logger.debug("Validating credentials for user: {}", username);

        if (username == null || password == null) {
            throw new NotAuthenticatedException("Username and password are required");
        }

        String userType = tryAuthenticate(username, password);
        logger.info("{} authenticated successfully: {}", userType, username);
        return userType;
    }

    private String tryAuthenticate(String username, String rawPassword) {
        if (isAuthenticated(TRAINEE, username, rawPassword,
                traineeRepository::findByUsername,
                trainee -> trainee.getUser().getPassword())) {
            return TRAINEE;
        }

        if (isAuthenticated(TRAINER, username, rawPassword,
                trainerRepository::findByUsername,
                trainer -> trainer.getUser().getPassword())) {
            return TRAINER;
        }

        throw new CoreServiceException("User not found: " + username);
    }

    private <T> boolean isAuthenticated(
            String userType,
            String username,
            String rawPassword,
            Function<String, Optional<T>> finder,
            Function<T, String> passwordExtractor) {

        Optional<T> userOptional = finder.apply(username);
        if (userOptional.isEmpty()) {
            return false;
        }

        T user = userOptional.get();
        String storedPassword = passwordExtractor.apply(user);

        if (!userCredentialsGenerator.matches(rawPassword, storedPassword)) {
            throw new NotAuthenticatedException(
                    String.format("Invalid password for %s: %s", userType.toLowerCase(), username)
            );
        }

        return true;
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
}

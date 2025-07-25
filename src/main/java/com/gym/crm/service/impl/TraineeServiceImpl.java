package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.transaction.PersistenceTx;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class TraineeServiceImpl implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private static final String TRAINEE_NOT_FOUND_MSG = "Trainee not found with username: ";

    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private UserCredentialsGenerator userCredentialsGenerator;
    private TraineeMapper traineeMapper;

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setUserCredentialsGenerator(UserCredentialsGenerator userCredentialsGenerator) {
        this.userCredentialsGenerator = userCredentialsGenerator;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Override
    @PersistenceTx
    public TraineeCreateResponseDto create(@Valid TraineeCreateRequestDto request) {
        logger.debug("Creating trainee: {} {}", request.getFirstName(), request.getLastName());

        Trainee trainee = traineeMapper.toEntity(request);

        List<String> existingUsernames = traineeDAO.findAll().stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                request.getFirstName(), request.getLastName(), existingUsernames);
        String rawPassword = userCredentialsGenerator.generateRawPassword();
        String encodedPassword = userCredentialsGenerator.encodePassword(rawPassword);

        User updatedUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .password(encodedPassword)
                .isActive(true)
                .build();
        trainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        Trainee saved = traineeDAO.create(trainee);

        logger.info("Successfully created trainee with ID: {} and username: {}", saved.getId(), saved.getUser().getUsername());

        return TraineeCreateResponseDto.builder()
                .username(username)
                .password(rawPassword)
                .build();
    }

    @Override
    public Optional<TraineeGetResponseDto> findById(Long id) {
        logger.debug("Finding trainee by ID: {}", id);

        return traineeDAO.findById(id)
                .map(traineeMapper::toResponse);
    }

    @Override
    public TraineeGetResponseDto findByUsername(String username) {
        logger.debug("Finding trainee by username: {}", username);

        Trainee trainee = traineeDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException("Unable to find trainee with username: " + username));

        return traineeMapper.toResponse(trainee);
    }

    @Override
    @PersistenceTx
    public TraineeUpdateResponseDto update(@Valid TraineeUpdateRequestDto request, String username) {
        logger.debug("Updating trainee with username: {}", username);

        Trainee existingTrainee = traineeDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));

        User updatedUser = existingTrainee.getUser().toBuilder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(request.getIsActive())
                .build();
        Trainee updatedTrainee = existingTrainee.toBuilder()
                .user(updatedUser)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        Trainee savedTrainee = traineeDAO.update(updatedTrainee);

        logger.info("Successfully updated trainee with username: {}", username);

        return traineeMapper.toUpdateResponseDto(savedTrainee);
    }

    @Override
    @PersistenceTx
    public TraineeTrainersUpdateResponseDto updateTraineeTrainersList(@Valid TraineeTrainersUpdateRequestDto request, String username) {
        logger.debug("Updating trainers list for trainee with username: {}", username);

        traineeDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));

        request.getTrainerUsernames().stream()
                .filter(trainerUsername -> trainerDAO.findByUsername(trainerUsername).isEmpty())
                .findFirst()
                .ifPresent(notFound -> {
                    throw new CoreServiceException("Trainer not found with username: " + notFound);
                });

        Trainee updatedTrainee = traineeDAO.updateTraineeTrainersList(username, request.getTrainerUsernames());

        logger.info("Successfully updated trainers list for trainee with username: {}", username);

        return traineeMapper.toTrainersUpdateResponse(updatedTrainee);
    }

    @Override
    @PersistenceTx
    public void deleteByUsername(String username) {
        logger.debug("Deleting trainee by username: {}", username);

        traineeDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));

        traineeDAO.deleteByUsername(username);

        logger.info("Trainee deleted with username: {}", username);
    }

    @Override
    public void changePassword(@Valid PasswordChangeRequest request) {
        logger.debug("Changing password for trainee: {}", request.getUsername());

        Trainee trainee = traineeDAO.findByUsername(request.getUsername())
                .orElseThrow(() -> new CoreServiceException("User not found with username: " + request.getUsername()));

        if (!userCredentialsGenerator.matches(request.getOldPassword(), trainee.getUser().getPassword())) {
            throw new CoreServiceException("Invalid old password");
        }

        String newPassword = userCredentialsGenerator.encodePassword(request.getNewPassword());

        User updatedUser = trainee.getUser().toBuilder()
                .password(newPassword)
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        traineeDAO.update(updatedTrainee);

        logger.info("Password changed successfully for trainee: {}", request.getUsername());
    }

    @Override
    @PersistenceTx
    public void toggleTraineeActivation(String username, boolean isActive) {
        logger.debug("Setting activation for trainee with username: {} to {}", username, isActive);

        Trainee trainee = traineeDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));

        User updatedUser = trainee.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        traineeDAO.update(updatedTrainee);

        logger.info("Successfully set activation for trainee with username: {} to {}", username, isActive ? "active" : "inactive");
    }
}

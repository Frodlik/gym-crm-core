package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingTypeDAO;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.AvailableTrainerResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateResponseDto;
import com.gym.crm.dto.trainer.TrainerGetResponseDto;
import com.gym.crm.dto.trainer.TrainerUpdateRequestDto;
import com.gym.crm.dto.trainer.TrainerUpdateResponseDto;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.transaction.PersistenceTx;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private static final String TRAINER_NOT_FOUND_MSG = "Trainer not found with username: ";

    private final TrainerDAO trainerDAO;
    private final TraineeDAO traineeDAO;
    private final TrainingTypeDAO trainingTypeDAO;
    private final UserCredentialsGenerator userCredentialsGenerator;
    private final TrainerMapper trainerMapper;

    @Override
    @PersistenceTx
    public TrainerCreateResponseDto create(@Valid TrainerCreateRequestDto request) {
        logger.debug("Creating trainer: {} {}", request.getFirstName(), request.getLastName());

        Trainer trainer = trainerMapper.toEntity(request);

        List<String> existingUsernames = trainerDAO.findAll().stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                request.getFirstName(), request.getLastName(), existingUsernames);
        String rawPassword = userCredentialsGenerator.generateRawPassword();
        String encodedPassword = userCredentialsGenerator.encodePassword(rawPassword);

        TrainingType specialization = trainingTypeDAO.findByName(request.getSpecialization().getTrainingTypeName())
                .orElseThrow(() -> new CoreServiceException("Training type not found: " + request.getSpecialization().getTrainingTypeName()));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .password(encodedPassword)
                .isActive(true)
                .build();
        trainer = trainer.toBuilder()
                .user(user)
                .specialization(specialization)
                .build();

        Trainer saved = trainerDAO.create(trainer);

        logger.info("Successfully created trainer with ID: {} and username: {}", saved.getId(), saved.getUser().getUsername());

        return TrainerCreateResponseDto.builder()
                .username(username)
                .password(rawPassword)
                .build();
    }

    @Override
    public Optional<TrainerGetResponseDto> findById(Long id) {
        logger.debug("Finding trainer by ID: {}", id);

        return trainerDAO.findById(id)
                .map(trainerMapper::toResponse);
    }

    @Override
    public TrainerGetResponseDto findByUsername(String username) {
        logger.debug("Finding trainer by username: {}", username);

        Trainer trainer = trainerDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINER_NOT_FOUND_MSG + username));

        return trainerMapper.toResponse(trainer);
    }

    @Override
    public List<AvailableTrainerResponseDto> findTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Finding trainers not assigned to trainee with username: {}", traineeUsername);

        traineeDAO.findByUsername(traineeUsername)
                .orElseThrow(() -> new CoreServiceException("Trainee not found with username: " + traineeUsername));

        List<Trainer> trainers = trainerDAO.findTrainersNotAssignedToTrainee(traineeUsername);

        logger.info("Found {} trainers not assigned to trainee: {}", trainers.size(), traineeUsername);

        return trainers.stream()
                .map(trainerMapper::toAvailableTrainerResponseDto)
                .toList();
    }

    @Override
    @PersistenceTx
    public TrainerUpdateResponseDto update(@Valid TrainerUpdateRequestDto request, String username) {
        logger.debug("Updating trainer with username: {}", username);

        Trainer existingTrainer = trainerDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINER_NOT_FOUND_MSG + username));

        TrainingType specialization = trainingTypeDAO.findByName(request.getSpecialization().getTrainingTypeName())
                .orElseThrow(() -> new CoreServiceException("Training type not found: " + request.getSpecialization().getTrainingTypeName()));

        User updatedUser = existingTrainer.getUser().toBuilder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(request.getIsActive())
                .build();
        Trainer trainer = existingTrainer.toBuilder()
                .user(updatedUser)
                .specialization(specialization)
                .build();

        Trainer updatedTrainer = trainerDAO.update(trainer);

        logger.info("Successfully updated trainer with username: {}", username);

        return trainerMapper.toUpdateResponseDto(updatedTrainer);
    }

    @Override
    public void changePassword(@Valid PasswordChangeRequest request) {
        logger.debug("Changing password for trainer: {}", request.getUsername());

        Trainer trainer = trainerDAO.findByUsername(request.getUsername())
                .orElseThrow(() -> new CoreServiceException("User not found with username: " + request.getUsername()));

        if (!userCredentialsGenerator.matches(request.getOldPassword(), trainer.getUser().getPassword())) {
            throw new CoreServiceException("Invalid old password");
        }

        String newPassword = userCredentialsGenerator.encodePassword(request.getNewPassword());

        User updatedUser = trainer.getUser().toBuilder()
                .password(newPassword)
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        trainerDAO.update(updatedTrainer);

        logger.info("Password changed successfully for trainer: {}", request.getUsername());
    }

    @Override
    @PersistenceTx
    public void toggleTrainerActivation(String username, boolean isActive) {
        logger.debug("Toggling activation for trainer with username: {}", username);

        Trainer trainer = trainerDAO.findByUsername(username)
                .orElseThrow(() -> new CoreServiceException(TRAINER_NOT_FOUND_MSG + username));
        boolean currentStatus = trainer.getUser().getIsActive();

        if (currentStatus == isActive) {
            throw new CoreServiceException(String.format("Trainer with username: %s is already %s", username, isActive ? "active" : "inactive"));
        }

        User updatedUser = trainer.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        trainerDAO.update(updatedTrainer);

        logger.info("Successfully set activation for trainer with username: {} to {}", username, isActive ? "active" : "inactive");
    }
}

package com.gym.crm.service.impl;

import com.gym.crm.actuator.prometheus.UserProfileMetrics;
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
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.service.TrainerService;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private static final String TRAINER_NOT_FOUND_MSG = "Trainer not found with username: ";

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserCredentialsGenerator userCredentialsGenerator;
    private final TrainerMapper trainerMapper;
    private final UserProfileMetrics userProfileMetrics;

    @Override
    @Transactional
    public TrainerCreateResponseDto create(@Valid TrainerCreateRequestDto request) {
        logger.debug("Creating trainer: {} {}", request.getFirstName(), request.getLastName());

        Trainer trainer = trainerMapper.toEntity(request);

        List<String> existingUsernames = trainerRepository.findAll().stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        String username = userCredentialsGenerator.generateUsername(
                request.getFirstName(), request.getLastName(), existingUsernames);
        String rawPassword = userCredentialsGenerator.generateRawPassword();
        String encodedPassword = userCredentialsGenerator.encodePassword(rawPassword);

        TrainingType specialization = trainingTypeRepository.findByTrainingTypeName(request.getSpecialization().getTrainingTypeName())
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

        Trainer saved = trainerRepository.save(trainer);

        logger.info("Successfully created trainer with ID: {} and username: {}", saved.getId(), saved.getUser().getUsername());
        userProfileMetrics.recordProfileCreation();

        return TrainerCreateResponseDto.builder()
                .username(username)
                .password(rawPassword)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerGetResponseDto> findById(Long id) {
        return trainerRepository.findById(id)
                .map(trainerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerGetResponseDto findByUsername(String username) {
        Trainer trainer = trainerRepository.findTrainerByUser_Username(username)
                .orElseThrow(() -> new CoreServiceException(TRAINER_NOT_FOUND_MSG + username));

        return trainerMapper.toResponse(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableTrainerResponseDto> findTrainersNotAssignedToTrainee(String traineeUsername) {
        traineeRepository.findTraineeByUser_Username(traineeUsername)
                .orElseThrow(() -> new CoreServiceException("Trainee not found with username: " + traineeUsername));

        List<Trainer> trainers = trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername);

        return trainers.stream()
                .map(trainerMapper::toAvailableTrainerResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public TrainerUpdateResponseDto update(@Valid TrainerUpdateRequestDto request, String username) {
        Trainer existingTrainer = trainerRepository.findTrainerByUser_Username(username)
                .orElseThrow(() -> new CoreServiceException(TRAINER_NOT_FOUND_MSG + username));

        TrainingType specialization = trainingTypeRepository.findByTrainingTypeName(request.getSpecialization().getTrainingTypeName())
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

        Trainer updatedTrainer = trainerRepository.save(trainer);

        userProfileMetrics.recordProfileUpdate();

        return trainerMapper.toUpdateResponseDto(updatedTrainer);
    }

    @Override
    @Transactional
    public void changePassword(@Valid PasswordChangeRequest request) {
        Trainer trainer = trainerRepository.findTrainerByUser_Username(request.getUsername())
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

        trainerRepository.save(updatedTrainer);
    }

    @Override
    @Transactional
    public void toggleTrainerActivation(String username, boolean isActive) {
        Trainer trainer = trainerRepository.findTrainerByUser_Username(username)
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

        trainerRepository.save(updatedTrainer);
    }
}

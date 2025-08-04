package com.gym.crm.service.impl;

import com.gym.crm.actuator.prometheus.UserProfileMetrics;
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
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.service.TraineeService;
import com.gym.crm.util.UserCredentialsGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private static final String TRAINEE_NOT_FOUND_MSG = "Trainee not found with username: ";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserCredentialsGenerator userCredentialsGenerator;
    private final TraineeMapper traineeMapper;
    private final UserProfileMetrics userProfileMetrics;

    @Override
    @Transactional
    public TraineeCreateResponseDto create(@Valid TraineeCreateRequestDto request) {
        logger.debug("Creating trainee: {} {}", request.getFirstName(), request.getLastName());

        Trainee trainee = traineeMapper.toEntity(request);

        List<String> existingUsernames = traineeRepository.findAll().stream()
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

        Trainee saved = traineeRepository.save(trainee);

        logger.info("Successfully created trainee with ID: {} and username: {}", saved.getId(), saved.getUser().getUsername());
        userProfileMetrics.recordProfileCreation();

        return TraineeCreateResponseDto.builder()
                .username(username)
                .password(rawPassword)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraineeGetResponseDto> findById(Long id) {
        return traineeRepository.findById(id)
                .map(traineeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TraineeGetResponseDto findByUsername(String username) {
        Trainee trainee = traineeRepository.findTraineeByUser_Username(username)
                .orElseThrow(() -> new CoreServiceException("Unable to find trainee with username: " + username));

        return traineeMapper.toResponse(trainee);
    }

    @Override
    @Transactional
    public TraineeUpdateResponseDto update(@Valid TraineeUpdateRequestDto request, String username) {

        Trainee existingTrainee = traineeRepository.findTraineeByUser_Username(username)
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

        Trainee savedTrainee = traineeRepository.save(updatedTrainee);

        userProfileMetrics.recordProfileUpdate();

        return traineeMapper.toUpdateResponseDto(savedTrainee);
    }

    @Override
    @Transactional
    public TraineeTrainersUpdateResponseDto updateTraineeTrainersList(@Valid TraineeTrainersUpdateRequestDto request, String username) {
        Trainee trainee = traineeRepository.findTraineeByUser_Username(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));
        Set<Trainer> trainers = findTrainersByUsernames(request.getTrainerUsernames());

        Trainee updatedTrainee = trainee.toBuilder()
                .trainers(trainers)
                .build();

        traineeRepository.save(updatedTrainee);

        userProfileMetrics.recordProfileUpdate();

        return traineeMapper.toTrainersUpdateResponse(updatedTrainee);
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        traineeRepository.findTraineeByUser_Username(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));

        traineeRepository.deleteByUser_Username(username);
    }

    @Override
    @Transactional
    public void changePassword(@Valid PasswordChangeRequest request) {
        Trainee trainee = traineeRepository.findTraineeByUser_Username(request.getUsername())
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

        traineeRepository.save(updatedTrainee);
    }

    @Override
    @Transactional
    public void toggleTraineeActivation(String username, boolean isActive) {
        Trainee trainee = traineeRepository.findTraineeByUser_Username(username)
                .orElseThrow(() -> new CoreServiceException(TRAINEE_NOT_FOUND_MSG + username));
        boolean currentStatus = trainee.getUser().getIsActive();

        if (currentStatus == isActive) {
            throw new CoreServiceException(String.format("Trainee with username: %s is already %s", username, isActive ? "active" : "inactive"));
        }

        User updatedUser = trainee.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        traineeRepository.save(updatedTrainee);
    }

    private Set<Trainer> findTrainersByUsernames(List<String> trainerUsernames) {
        List<Trainer> foundTrainers = trainerRepository.findAllByUser_UsernameIn(trainerUsernames);

        Set<String> foundUsernames = foundTrainers.stream()
                .map(trainer -> trainer.getUser().getUsername())
                .collect(Collectors.toSet());
        List<String> missingUsernames = trainerUsernames.stream()
                .filter(username -> !foundUsernames.contains(username))
                .toList();

        if (!missingUsernames.isEmpty()) {
            String missingUsernamesStr = String.join(", ", missingUsernames);
            throw new CoreServiceException("Trainers not found with usernames: " + missingUsernamesStr);
        }

        return new HashSet<>(foundTrainers);
    }
}

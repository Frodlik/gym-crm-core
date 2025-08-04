package com.gym.crm.service.impl;

import com.gym.crm.actuator.prometheus.UserProfileMetrics;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.AvailableTrainerResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateResponseDto;
import com.gym.crm.dto.trainer.TrainerGetResponseDto;
import com.gym.crm.dto.trainer.TrainerUpdateRequestDto;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.buildTrainerResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    private static final String TRAINER_FIRST_NAME = "Mike";
    private static final String TRAINER_LAST_NAME = "Johnson";
    private static final String TRAINER_USERNAME = "mike.johnson";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String FITNESS_TYPE = "Fitness";
    private static final Long TRAINER_ID = 1L;
    private static final String RAW_PASSWORD = "rawPassword123";

    @Captor
    private ArgumentCaptor<Trainer> trainerCaptor;

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private UserProfileMetrics userProfileMetrics;
    @InjectMocks
    private TrainerServiceImpl service;

    @Test
    void create_ShouldCreateTrainerSuccessfully() {
        TrainerCreateRequestDto createRequest = GymTestObjects.buildTrainerCreateRequest();
        Trainer initialTrainer = buildTrainer();
        List<Trainer> existingTrainers = List.of(
                createTrainerWithUsername("existing.trainer1"),
                createTrainerWithUsername("existing.trainer2")
        );
        List<String> existingUsernames = List.of("existing.trainer1", "existing.trainer2");
        TrainingType specialization = buildTrainingType();
        Trainer savedTrainer = buildTrainerWithId();
        TrainerCreateResponseDto expected = TrainerCreateResponseDto.builder()
                .username(TRAINER_USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(trainerMapper.toEntity(createRequest)).thenReturn(initialTrainer);
        when(trainerRepository.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeRepository.findByTrainingTypeName(createRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.of(specialization));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(savedTrainer);

        TrainerCreateResponseDto actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
        verify(trainerMapper).toEntity(createRequest);
        verify(trainerRepository).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generateRawPassword();
        verify(userCredentialsGenerator).encodePassword(RAW_PASSWORD);
        verify(trainingTypeRepository).findByTrainingTypeName(createRequest.getSpecialization().getTrainingTypeName());
        verify(trainerRepository).save(any(Trainer.class));
        verify(userProfileMetrics).recordProfileCreation();
    }

    @Test
    void create_ShouldHandleEmptyExistingUsernames() {
        TrainerCreateRequestDto createRequest = GymTestObjects.buildTrainerCreateRequest();
        List<String> existingUsernames = List.of();
        TrainingType specialization = buildTrainingType();
        Trainer savedTrainer = buildTrainerWithId();
        TrainerCreateResponseDto expectedResponse = TrainerCreateResponseDto.builder()
                .username(TRAINER_USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(trainerMapper.toEntity(createRequest)).thenReturn(buildTrainer());
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeRepository.findByTrainingTypeName(createRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.of(specialization));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(savedTrainer);

        TrainerCreateResponseDto actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expectedResponse.getUsername(), actual.getUsername());
        assertEquals(expectedResponse.getPassword(), actual.getPassword());
        verify(trainerRepository).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generateRawPassword();
        verify(userCredentialsGenerator).encodePassword(RAW_PASSWORD);
        verify(trainingTypeRepository).findByTrainingTypeName(createRequest.getSpecialization().getTrainingTypeName());
        verify(userProfileMetrics).recordProfileCreation();
    }

    @Test
    void create_ShouldThrowException_WhenTrainingTypeNotFound() {
        TrainerCreateRequestDto createRequest = GymTestObjects.buildTrainerCreateRequest();

        when(trainerMapper.toEntity(createRequest)).thenReturn(buildTrainer());
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(userCredentialsGenerator.generateUsername(any(), any(), any())).thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeRepository.findByTrainingTypeName(createRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.create(createRequest));

        assertEquals("Training type not found: " + createRequest.getSpecialization().getTrainingTypeName(),
                exception.getMessage());
        verify(trainerRepository, never()).save(any());
        verify(userProfileMetrics, never()).recordProfileCreation();
    }

    @Test
    void findById_ShouldReturnTrainer_WhenExists() {
        Trainer trainer = buildTrainer();
        TrainerGetResponseDto expected = buildTrainerResponse();

        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Optional<TrainerGetResponseDto> actual = service.findById(TRAINER_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getFirstName(), actual.get().getFirstName());
        assertEquals(expected.getSpecialization(), actual.get().getSpecialization());
        verify(trainerRepository).findById(TRAINER_ID);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Long trainerId = 999L;
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.empty());

        Optional<TrainerGetResponseDto> result = service.findById(trainerId);

        assertFalse(result.isPresent());
        verify(trainerRepository).findById(trainerId);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void findByUsername_ShouldReturnTrainer_WhenExists() {
        Trainer trainer = buildTrainer();
        TrainerGetResponseDto expected = buildTrainerResponse();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        TrainerGetResponseDto actual = service.findByUsername(TRAINER_USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findByUsername_ShouldThrowException_WhenNotExists() {
        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.findByUsername(TRAINER_USERNAME));

        assertEquals("Trainer not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldThrowException_WhenTrainingTypeNotFound() {
        TrainerUpdateRequestDto updateRequest = GymTestObjects.buildTrainerUpdateRequest();
        String username = "test.username";
        Trainer originalTrainer = buildTrainer();

        when(trainerRepository.findTrainerByUser_Username(username)).thenReturn(Optional.of(originalTrainer));
        when(trainingTypeRepository.findByTrainingTypeName(updateRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.update(updateRequest, username));

        assertEquals("Training type not found: " + updateRequest.getSpecialization().getTrainingTypeName(),
                exception.getMessage());
        verify(trainerRepository, never()).save(any());
        verify(userProfileMetrics, never()).recordProfileUpdate();
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenOldPasswordMatches() {
        PasswordChangeRequest request = buildPasswordChangeRequest();
        Trainer trainer = buildTrainer();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(userCredentialsGenerator.matches(PASSWORD, trainer.getUser().getPassword())).thenReturn(true);
        when(userCredentialsGenerator.encodePassword("newSecurePassword")).thenReturn("newSecurePassword");

        service.changePassword(request);

        verify(trainerRepository).save(trainerCaptor.capture());
        Trainer updated = trainerCaptor.getValue();
        assertEquals("newSecurePassword", updated.getUser().getPassword());
        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void changePassword_ShouldThrowException_WhenUserNotFound() {
        PasswordChangeRequest request = buildPasswordChangeRequest();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.changePassword(request));

        assertEquals("User not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordInvalid() {
        PasswordChangeRequest request = buildPasswordChangeRequest();
        Trainer trainer = buildTrainer();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.changePassword(request));

        assertEquals("Invalid old password", exception.getMessage());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void toggleTrainerActivation_ShouldToggleFromActiveToInactive() {
        Trainer existingTrainer = buildTrainer();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(existingTrainer));

        service.toggleTrainerActivation(TRAINER_USERNAME, false);

        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerRepository).save(trainerCaptor.capture());
        Trainer captured = trainerCaptor.getValue();
        assertFalse(captured.getUser().getIsActive());
    }

    @Test
    void toggleTrainerActivation_ShouldToggleFromInactiveToActive() {
        Trainer inactiveTrainer = buildInactiveTrainer();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(inactiveTrainer));

        service.toggleTrainerActivation(TRAINER_USERNAME, true);

        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerRepository).save(trainerCaptor.capture());
        Trainer captured = trainerCaptor.getValue();
        assertTrue(captured.getUser().getIsActive());
    }

    @Test
    void toggleTrainerActivation_ShouldThrowException_WhenTrainerNotFound() {
        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTrainerActivation(TRAINER_USERNAME, true));

        assertEquals("Trainer not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void toggleTrainerActivation_ShouldThrowException_WhenTryingToActivateAlreadyActiveTrainer() {
        String expectedMessage = String.format("Trainer with username: %s is already active", TRAINER_USERNAME);
        Trainer activeTrainer = buildTrainer();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(activeTrainer));

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTrainerActivation(TRAINER_USERNAME, true));

        assertEquals(expectedMessage, exception.getMessage());
        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void toggleTrainerActivation_ShouldThrowException_WhenTryingToDeactivateAlreadyInactiveTrainer() {
        String expectedMessage = String.format("Trainer with username: %s is already inactive", TRAINER_USERNAME);
        Trainer inactiveTrainer = buildInactiveTrainer();

        when(trainerRepository.findTrainerByUser_Username(TRAINER_USERNAME)).thenReturn(Optional.of(inactiveTrainer));

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTrainerActivation(TRAINER_USERNAME, false));

        assertEquals(expectedMessage, exception.getMessage());
        verify(trainerRepository).findTrainerByUser_Username(TRAINER_USERNAME);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void findTrainersNotAssignedToTrainee_ShouldReturnListOfTrainers() {
        String traineeUsername = "trainee.user";
        Trainee trainee = buildTraineeWithUsername(traineeUsername);
        List<Trainer> unassignedTrainers = List.of(
                createTrainerWithUsername("trainer1"),
                createTrainerWithUsername("trainer2"),
                createTrainerWithUsername("trainer3")
        );
        List<AvailableTrainerResponseDto> expectedResponses = List.of(
                createAvailableTrainerResponse("trainer1"),
                createAvailableTrainerResponse("trainer2"),
                createAvailableTrainerResponse("trainer3")
        );

        when(traineeRepository.findTraineeByUser_Username(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findTrainersNotAssignedToTrainee(traineeUsername)).thenReturn(unassignedTrainers);
        when(trainerMapper.toAvailableTrainerResponseDto(unassignedTrainers.get(0))).thenReturn(expectedResponses.get(0));
        when(trainerMapper.toAvailableTrainerResponseDto(unassignedTrainers.get(1))).thenReturn(expectedResponses.get(1));
        when(trainerMapper.toAvailableTrainerResponseDto(unassignedTrainers.get(2))).thenReturn(expectedResponses.get(2));

        List<AvailableTrainerResponseDto> actual = service.findTrainersNotAssignedToTrainee(traineeUsername);

        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertEquals("trainer1", actual.get(0).getUsername());
        assertEquals("trainer2", actual.get(1).getUsername());
        assertEquals("trainer3", actual.get(2).getUsername());
        verify(traineeRepository).findTraineeByUser_Username(traineeUsername);
        verify(trainerRepository).findTrainersNotAssignedToTrainee(traineeUsername);
        verify(trainerMapper).toAvailableTrainerResponseDto(unassignedTrainers.get(0));
        verify(trainerMapper).toAvailableTrainerResponseDto(unassignedTrainers.get(1));
        verify(trainerMapper).toAvailableTrainerResponseDto(unassignedTrainers.get(2));
    }

    @Test
    void findTrainersNotAssignedToTrainee_ShouldThrowException_WhenTraineeNotFound() {
        String traineeUsername = "nonexistent.trainee";

        when(traineeRepository.findTraineeByUser_Username(traineeUsername)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.findTrainersNotAssignedToTrainee(traineeUsername));

        assertEquals("Trainee not found with username: " + traineeUsername, exception.getMessage());
        verify(trainerRepository, never()).findTrainersNotAssignedToTrainee(any());
    }

    private Trainer buildTrainer() {
        User user = buildUser();
        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(buildTrainingType())
                .build();
    }

    private Trainer buildTrainerWithId() {
        return buildTrainer().toBuilder()
                .id(TrainerServiceImplTest.TRAINER_ID)
                .build();
    }

    private Trainer buildInactiveTrainer() {
        User inactiveUser = buildUser().toBuilder()
                .isActive(false)
                .build();
        return buildTrainer().toBuilder()
                .user(inactiveUser)
                .build();
    }

    private User buildUser() {
        return User.builder()
                .id(999L)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();
    }

    private TrainingType buildTrainingType() {
        return TrainingType.builder()
                .trainingTypeName(TrainerServiceImplTest.FITNESS_TYPE)
                .build();
    }

    private Trainer createTrainerWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .build();

        return Trainer.builder()
                .user(user)
                .build();
    }

    private Trainee buildTraineeWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .build();

        return Trainee.builder()
                .user(user)
                .build();
    }

    private AvailableTrainerResponseDto createAvailableTrainerResponse(String username) {
        AvailableTrainerResponseDto response = new AvailableTrainerResponseDto();
        response.setUsername(username);
        response.setFirstName("Trainer");
        response.setLastName("Name");
        response.setSpecialization(buildTrainingType());

        return response;
    }

    private PasswordChangeRequest buildPasswordChangeRequest() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(TRAINER_USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword("newSecurePassword");

        return request;
    }
}

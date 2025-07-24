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
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
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
    private static final String YOGA_TYPE = "YOGA";
    private static final Long TRAINER_ID = 1L;
    private static final String RAW_PASSWORD = "rawPassword123";

    private final Trainer trainer = buildTrainer();
    @Captor
    private ArgumentCaptor<Trainer> captor;

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainingTypeDAO trainingTypeDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TrainerMapper trainerMapper;
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

        TrainingType specialization = TrainingType.builder()
                .trainingTypeName(FITNESS_TYPE)
                .build();
        Trainer savedTrainer = trainer.toBuilder()
                .id(TRAINER_ID)
                .build();
        TrainerCreateResponseDto expected = TrainerCreateResponseDto.builder()
                .username(TRAINER_USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(trainerMapper.toEntity(createRequest)).thenReturn(initialTrainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeDAO.findByName(createRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.of(specialization));
        when(trainerDAO.create(any(Trainer.class))).thenReturn(savedTrainer);

        TrainerCreateResponseDto actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());

        verify(trainerMapper).toEntity(createRequest);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generateRawPassword();
        verify(userCredentialsGenerator).encodePassword(RAW_PASSWORD);
        verify(trainingTypeDAO).findByName(createRequest.getSpecialization().getTrainingTypeName());
        verify(trainerDAO).create(any(Trainer.class));
    }

    @Test
    void create_ShouldHandleEmptyExistingUsernames() {
        TrainerCreateRequestDto createRequest = GymTestObjects.buildTrainerCreateRequest();
        List<Trainer> existingTrainers = List.of();
        List<String> existingUsernames = List.of();

        TrainingType specialization = TrainingType.builder()
                .trainingTypeName(FITNESS_TYPE)
                .build();
        Trainer savedTrainer = trainer.toBuilder()
                .id(TRAINER_ID)
                .build();
        TrainerCreateResponseDto expectedResponse = TrainerCreateResponseDto.builder()
                .username(TRAINER_USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeDAO.findByName(createRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.of(specialization));
        when(trainerDAO.create(any(Trainer.class))).thenReturn(savedTrainer);

        TrainerCreateResponseDto actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expectedResponse.getUsername(), actual.getUsername());
        assertEquals(expectedResponse.getPassword(), actual.getPassword());
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generateRawPassword();
        verify(userCredentialsGenerator).encodePassword(RAW_PASSWORD);
        verify(trainingTypeDAO).findByName(createRequest.getSpecialization().getTrainingTypeName());
    }

    @Test
    void create_ShouldThrowExceptionWhenTrainingTypeNotFound() {
        TrainerCreateRequestDto createRequest = GymTestObjects.buildTrainerCreateRequest();
        List<Trainer> existingTrainers = List.of();

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(any(), any(), any())).thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeDAO.findByName(createRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(createRequest));

        assertEquals("Training type not found: " + createRequest.getSpecialization().getTrainingTypeName(),
                exception.getMessage());
        verify(trainerDAO, never()).create(any());
    }

    @Test
    void findById_ShouldReturnTrainerWhenExists() {
        TrainerGetResponseDto expected = buildTrainerResponse();

        when(trainerDAO.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Optional<TrainerGetResponseDto> actual = service.findById(TRAINER_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getFirstName(), actual.get().getFirstName());
        assertEquals(expected.getSpecialization(), actual.get().getSpecialization());
        verify(trainerDAO).findById(TRAINER_ID);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainerId = 999L;

        when(trainerDAO.findById(trainerId)).thenReturn(Optional.empty());

        Optional<TrainerGetResponseDto> result = service.findById(trainerId);

        assertFalse(result.isPresent());
        verify(trainerDAO).findById(trainerId);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void findByUsername_ShouldReturnTrainerWhenExists() {
        Trainer buildTrainer = buildTrainer();
        TrainerGetResponseDto expected = buildTrainerResponse();

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(buildTrainer));
        when(trainerMapper.toResponse(buildTrainer)).thenReturn(expected);

        TrainerGetResponseDto actual = service.findByUsername(TRAINER_USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerMapper).toResponse(buildTrainer);
    }

    @Test
    void findByUsername_ShouldThrowExceptionWhenNotExists() {
        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.findByUsername(TRAINER_USERNAME));

        assertEquals("Trainer not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTrainerSuccessfully() {
        TrainerUpdateRequestDto updateRequest = GymTestObjects.buildTrainerUpdateRequest();
        String username = "test.username";
        Trainer originalTrainer = buildTrainer();
        Trainer updatedTrainer = buildUpdatedTrainer();

        TrainerUpdateResponseDto expected = new TrainerUpdateResponseDto();
        expected.setFirstName("Michael");
        expected.setLastName("Smith");
        expected.setActive(false);
        expected.setSpecialization(YOGA_TYPE);

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(originalTrainer));
        when(trainerDAO.update(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toUpdateResponseDto(updatedTrainer)).thenReturn(expected);
        when(trainingTypeDAO.findByName(updateRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.of(TrainingType.builder().trainingTypeName(YOGA_TYPE).build()));

        TrainerUpdateResponseDto actual = service.update(updateRequest, username);

        assertNotNull(actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.isActive(), actual.isActive());
        assertEquals(expected.getSpecialization(), actual.getSpecialization());

        verify(trainerDAO).findByUsername(username);
        verify(trainingTypeDAO).findByName(updateRequest.getSpecialization().getTrainingTypeName());
        verify(trainerDAO).update(any(Trainer.class));
        verify(trainerMapper).toUpdateResponseDto(updatedTrainer);
    }

    @Test
    void update_ShouldThrowExceptionWhenTrainerNotFound() {
        TrainerUpdateRequestDto updateRequest = GymTestObjects.buildTrainerUpdateRequest();
        String username = "nonexistent.username";

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.update(updateRequest, username));

        assertEquals("Trainer not found with username: " + username, exception.getMessage());
        verify(trainerDAO).findByUsername(username);
        verify(trainerDAO, never()).update(any());
        verify(trainerMapper, never()).toUpdateResponseDto(any());
    }

    @Test
    void update_ShouldThrowExceptionWhenTrainingTypeNotFound() {
        TrainerUpdateRequestDto updateRequest = GymTestObjects.buildTrainerUpdateRequest();
        String username = "test.username";
        Trainer originalTrainer = buildTrainer();

        when(trainerDAO.findByUsername(username)).thenReturn(Optional.of(originalTrainer));
        when(trainingTypeDAO.findByName(updateRequest.getSpecialization().getTrainingTypeName()))
                .thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.update(updateRequest, username));

        assertEquals("Training type not found: " + updateRequest.getSpecialization().getTrainingTypeName(),
                exception.getMessage());
        verify(trainerDAO, never()).update(any());
    }

    @Test
    void changePassword_ShouldUpdatePasswordWhenOldPasswordMatches() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(TRAINER_USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword("newSecurePassword");

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        service.changePassword(request);

        verify(trainerDAO).update(captor.capture());

        Trainer updated = captor.getValue();
        assertEquals("newSecurePassword", updated.getUser().getPassword());
        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerDAO).update(any(Trainer.class));
    }

    @Test
    void changePassword_ShouldThrowExceptionWhenUserNotFound() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(TRAINER_USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword("newSecurePassword");

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.changePassword(request));

        assertEquals("User not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(trainerDAO, never()).update(any());
    }

    @Test
    void changePassword_ShouldThrowExceptionWhenOldPasswordInvalid() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(TRAINER_USERNAME);
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newSecurePassword");

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.changePassword(request));

        assertEquals("Invalid old password", exception.getMessage());
        verify(trainerDAO, never()).update(any());
    }

    @Test
    void toggleTrainerActivation_ShouldToggleFromActiveToInactive() {
        Trainer existingTrainer = buildTrainer();
        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(existingTrainer));

        service.toggleTrainerActivation(TRAINER_USERNAME, false);

        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerDAO).update(captor.capture());

        Trainer captured = captor.getValue();
        assertFalse(captured.getUser().getIsActive());
    }

    @Test
    void toggleTrainerActivation_ShouldToggleFromInactiveToActive() {
        User inactiveUser = buildTrainer().getUser().toBuilder()
                .isActive(false)
                .build();
        Trainer inactiveTrainer = buildTrainer().toBuilder()
                .user(inactiveUser)
                .build();

        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(inactiveTrainer));

        service.toggleTrainerActivation(TRAINER_USERNAME, true);

        verify(trainerDAO).findByUsername(TRAINER_USERNAME);
        verify(trainerDAO).update(captor.capture());

        Trainer captured = captor.getValue();
        assertTrue(captured.getUser().getIsActive());
    }

    @Test
    void toggleTrainerActivation_ShouldThrowExceptionWhenTrainerNotFound() {
        when(trainerDAO.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTrainerActivation(TRAINER_USERNAME, true));

        assertEquals("Trainer not found with username: " + TRAINER_USERNAME, exception.getMessage());
        verify(trainerDAO, never()).update(any());
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

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainerDAO.findTrainersNotAssignedToTrainee(traineeUsername)).thenReturn(unassignedTrainers);
        when(trainerMapper.toAvailableTrainerResponseDto(unassignedTrainers.get(0))).thenReturn(expectedResponses.get(0));
        when(trainerMapper.toAvailableTrainerResponseDto(unassignedTrainers.get(1))).thenReturn(expectedResponses.get(1));
        when(trainerMapper.toAvailableTrainerResponseDto(unassignedTrainers.get(2))).thenReturn(expectedResponses.get(2));

        List<AvailableTrainerResponseDto> actual = service.findTrainersNotAssignedToTrainee(traineeUsername);

        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertEquals("trainer1", actual.get(0).getUsername());
        assertEquals("trainer2", actual.get(1).getUsername());
        assertEquals("trainer3", actual.get(2).getUsername());

        verify(traineeDAO).findByUsername(traineeUsername);
        verify(trainerDAO).findTrainersNotAssignedToTrainee(traineeUsername);
        verify(trainerMapper).toAvailableTrainerResponseDto(unassignedTrainers.get(0));
        verify(trainerMapper).toAvailableTrainerResponseDto(unassignedTrainers.get(1));
        verify(trainerMapper).toAvailableTrainerResponseDto(unassignedTrainers.get(2));
    }

    @Test
    void findTrainersNotAssignedToTrainee_ShouldThrowExceptionWhenTraineeNotFound() {
        String traineeUsername = "nonexistent.trainee";

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.findTrainersNotAssignedToTrainee(traineeUsername));

        assertEquals("Trainee not found with username: " + traineeUsername, exception.getMessage());
        verify(trainerDAO, never()).findTrainersNotAssignedToTrainee(any());
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(999L)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build())
                .build();
    }

    private Trainer buildUpdatedTrainer() {
        User user = buildTrainer().getUser().toBuilder()
                .firstName("Michael")
                .lastName("Smith")
                .isActive(false)
                .build();

        return buildTrainer().toBuilder()
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build())
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
        response.setSpecialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build());

        return response;
    }
}

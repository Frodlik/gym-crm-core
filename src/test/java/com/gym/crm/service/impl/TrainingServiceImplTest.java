package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dao.TrainingTypeDAO;
import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.dto.training.TrainingCreateRequestDto;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String TRAINER_FIRST_NAME = "Mike";
    private static final String TRAINER_LAST_NAME = "Johnson";
    private static final String TRAINER_USERNAME = "mike.johnson";
    private static final String TRAINING_NAME = "Morning Workout";
    private static final String FITNESS_TYPE = "Fitness";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    private static final int TRAINING_DURATION = 60;
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final Long TRAINING_ID = 1L;

    private final Training training = buildTraining();
    private final Trainee trainee = buildTrainee();
    private final Trainer trainer = buildTrainer();

    @Mock
    private TrainingDAO trainingDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TrainingTypeDAO trainingTypeDAO;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void create_ShouldCreateTrainingSuccessfully() {
        TrainingCreateRequestDto createRequest = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findByUsername(createRequest.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername(createRequest.getTrainerUsername())).thenReturn(Optional.of(trainer));
        when(trainingTypeDAO.findByName(createRequest.getTrainingName())).thenReturn(Optional.of(buildFitnessTrainingType()));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);

        service.create(createRequest);

        verify(traineeDAO).findByUsername(createRequest.getTraineeUsername());
        verify(trainerDAO).findByUsername(createRequest.getTrainerUsername());
        verify(trainingMapper).toEntity(createRequest);
        verify(trainingDAO).create(any(Training.class));
    }

    @Test
    void create_ShouldThrowExceptionWhenTraineeNotFound() {
        TrainingCreateRequestDto request = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findByUsername(request.getTraineeUsername())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.create(request));

        assertEquals("Trainee not found with username: " + request.getTraineeUsername(), exception.getMessage());

        verify(traineeDAO).findByUsername(request.getTraineeUsername());
        verifyNoMoreInteractions(trainerDAO, trainingMapper, trainingDAO);
    }

    @Test
    void create_ShouldThrowExceptionWhenTrainerNotFound() {
        TrainingCreateRequestDto request = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findByUsername(request.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername(request.getTrainerUsername())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.create(request));

        assertEquals("Trainer not found with username: " + request.getTrainerUsername(), exception.getMessage());

        verify(traineeDAO).findByUsername(request.getTraineeUsername());
        verify(trainerDAO).findByUsername(request.getTrainerUsername());
        verifyNoMoreInteractions(trainingMapper, trainingDAO);
    }

    @Test
    void create_ShouldThrowExceptionWhenTrainingTypeNotFound() {
        TrainingCreateRequestDto request = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findByUsername(request.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername(request.getTrainerUsername())).thenReturn(Optional.of(trainer));
        when(trainingTypeDAO.findByName(request.getTrainingName())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.create(request));

        assertEquals("Training type not found with name: " + request.getTrainingName(), exception.getMessage());

        verify(traineeDAO).findByUsername(request.getTraineeUsername());
        verify(trainerDAO).findByUsername(request.getTrainerUsername());
        verify(trainingTypeDAO).findByName(request.getTrainingName());
        verifyNoMoreInteractions(trainingMapper, trainingDAO);
    }

    @Test
    void findById_ShouldReturnTrainingWhenExists() {
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainingDAO.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        Optional<TrainingResponse> actual = service.findById(TRAINING_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getTraineeName(), actual.get().getTraineeName());
        assertEquals(expected.getTrainerName(), actual.get().getTrainerName());
        assertEquals(expected.getTrainingName(), actual.get().getTrainingName());

        verify(trainingDAO).findById(TRAINING_ID);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainingId = 999L;

        when(trainingDAO.findById(trainingId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = service.findById(trainingId);

        assertFalse(result.isPresent());
        verify(trainingDAO).findById(trainingId);
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldSetCorrectUserIds() {
        Long traineeUserId = 100L;
        Long trainerUserId = 200L;
        TrainingCreateRequestDto createRequest = GymTestObjects.buildTrainingCreateRequest();

        Trainee updatedTrainee = trainee.toBuilder()
                .id(traineeUserId)
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .id(trainerUserId)
                .build();
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);

        when(traineeDAO.findByUsername(createRequest.getTraineeUsername())).thenReturn(Optional.of(updatedTrainee));
        when(trainerDAO.findByUsername(createRequest.getTrainerUsername())).thenReturn(Optional.of(updatedTrainer));
        when(trainingTypeDAO.findByName(createRequest.getTrainingName())).thenReturn(Optional.of(buildFitnessTrainingType()));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);

        service.create(createRequest);

        verify(trainingDAO).create(captor.capture());

        Training captured = captor.getValue();
        assertEquals(traineeUserId, captured.getTrainee().getId());
        assertEquals(trainerUserId, captured.getTrainer().getId());
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldReturnMappedResponses() {
        String traineeUsername = USERNAME;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String trainerName = "Mike";
        String trainingType = "Fitness";

        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername(USERNAME)
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .trainerName("Mike")
                .trainingType("Fitness")
                .build();

        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainingDAO.findTraineeTrainingsByCriteria(traineeUsername, from, to, trainerName, trainingType))
                .thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        List<TrainingResponse> actual = service.getTraineeTrainingsByCriteria(filter);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
        verify(traineeDAO).findByUsername(traineeUsername);
        verify(trainingDAO).findTraineeTrainingsByCriteria(traineeUsername, from, to, trainerName, trainingType);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldThrow_WhenTraineeNotFound() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername("nonexistent")
                .build();

        when(traineeDAO.findByUsername("nonexistent")).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () ->
                service.getTraineeTrainingsByCriteria(filter));

        assertEquals("Trainee not found with username: nonexistent", exception.getMessage());
        verify(traineeDAO).findByUsername("nonexistent");
        verifyNoInteractions(trainingDAO);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldReturnMappedResponses() {
        String trainerUsername = TRAINER_USERNAME;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String traineeName = "John";

        TrainerSearchFilter filter = TrainerSearchFilter.builder()
                .trainerUsername(TRAINER_USERNAME)
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .traineeName("John")
                .build();

        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainerDAO.findByUsername(trainerUsername)).thenReturn(Optional.of(trainer));
        when(trainingDAO.findTrainerTrainingsByCriteria(trainerUsername, from, to, traineeName))
                .thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        List<TrainingResponse> actual = service.getTrainerTrainingsByCriteria(filter);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
        verify(trainerDAO).findByUsername(trainerUsername);
        verify(trainingDAO).findTrainerTrainingsByCriteria(trainerUsername, from, to, traineeName);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldThrow_WhenTrainerNotFound() {
        TrainerSearchFilter filter = TrainerSearchFilter.builder()
                .trainerUsername("not.found")
                .build();

        when(trainerDAO.findByUsername("not.found")).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () ->
                service.getTrainerTrainingsByCriteria(filter));

        assertEquals("Trainer not found with username: not.found", exception.getMessage());
        verify(trainerDAO).findByUsername("not.found");
        verifyNoInteractions(trainingDAO);
    }

    private Training buildTraining() {
        return Training.builder()
                .id(TRAINEE_ID)
                .id(TRAINER_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .trainingType(buildFitnessTrainingType())
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(999L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main St")
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(999L)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .password("password123")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(buildFitnessTrainingType())
                .build();
    }

    private TrainingType buildFitnessTrainingType() {
        return TrainingType.builder()
                .trainingTypeName(FITNESS_TYPE)
                .build();
    }
}
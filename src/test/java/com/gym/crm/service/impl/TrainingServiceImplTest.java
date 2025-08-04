package com.gym.crm.service.impl;

import com.gym.crm.actuator.prometheus.TrainingMetrics;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
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
import org.springframework.data.jpa.domain.Specification;

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

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private TrainingMetrics trainingMetrics;
    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void create_ShouldCreateTrainingSuccessfully() {
        TrainingCreateRequestDto createRequest = GymTestObjects.buildTrainingCreateRequest();
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();
        TrainingType trainingType = buildFitnessTrainingType();
        Training training = buildTraining();

        when(traineeRepository.findTraineeByUser_Username(createRequest.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findTrainerByUser_Username(createRequest.getTrainerUsername())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(createRequest.getTrainingName())).thenReturn(Optional.of(trainingType));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);

        service.create(createRequest);

        verify(traineeRepository).findTraineeByUser_Username(createRequest.getTraineeUsername());
        verify(trainerRepository).findTrainerByUser_Username(createRequest.getTrainerUsername());
        verify(trainingTypeRepository).findByTrainingTypeName(createRequest.getTrainingName());
        verify(trainingMapper).toEntity(createRequest);
        verify(trainingRepository).save(any(Training.class));
        verify(trainingMetrics).recordTrainingCreated();
    }

    @Test
    void create_ShouldThrowException_WhenTraineeNotFound() {
        TrainingCreateRequestDto request = GymTestObjects.buildTrainingCreateRequest();
        when(traineeRepository.findTraineeByUser_Username(request.getTraineeUsername())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(request));

        assertEquals("Trainee not found with username: " + request.getTraineeUsername(), exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(request.getTraineeUsername());
        verifyNoMoreInteractions(trainerRepository, trainingTypeRepository, trainingMapper, trainingRepository);
        verify(trainingMetrics, never()).recordTrainingCreated();
    }

    @Test
    void create_ShouldThrowException_WhenTrainerNotFound() {
        TrainingCreateRequestDto request = GymTestObjects.buildTrainingCreateRequest();
        Trainee trainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(request.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findTrainerByUser_Username(request.getTrainerUsername())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(request));

        assertEquals("Trainer not found with username: " + request.getTrainerUsername(), exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(request.getTraineeUsername());
        verify(trainerRepository).findTrainerByUser_Username(request.getTrainerUsername());
        verifyNoMoreInteractions(trainingTypeRepository, trainingMapper, trainingRepository);
        verify(trainingMetrics, never()).recordTrainingCreated();
    }

    @Test
    void create_ShouldThrowException_WhenTrainingTypeNotFound() {
        TrainingCreateRequestDto request = GymTestObjects.buildTrainingCreateRequest();
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();

        when(traineeRepository.findTraineeByUser_Username(request.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findTrainerByUser_Username(request.getTrainerUsername())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(request.getTrainingName())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(request));

        assertEquals("Training type not found with name: " + request.getTrainingName(), exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(request.getTraineeUsername());
        verify(trainerRepository).findTrainerByUser_Username(request.getTrainerUsername());
        verify(trainingTypeRepository).findByTrainingTypeName(request.getTrainingName());
        verifyNoMoreInteractions(trainingMapper, trainingRepository);
        verify(trainingMetrics, never()).recordTrainingCreated();
    }

    @Test
    void findById_ShouldReturnTraining_WhenExists() {
        Training training = buildTraining();
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainingRepository.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        Optional<TrainingResponse> actual = service.findById(TRAINING_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getTraineeName(), actual.get().getTraineeName());
        assertEquals(expected.getTrainerName(), actual.get().getTrainerName());
        assertEquals(expected.getTrainingName(), actual.get().getTrainingName());
        verify(trainingRepository).findById(TRAINING_ID);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Long trainingId = 999L;
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = service.findById(trainingId);

        assertFalse(result.isPresent());
        verify(trainingRepository).findById(trainingId);
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldSetCorrectUserIds() {
        Long traineeUserId = 100L;
        Long trainerUserId = 200L;
        TrainingCreateRequestDto createRequest = GymTestObjects.buildTrainingCreateRequest();
        Trainee trainee = buildTraineeWithId(traineeUserId);
        Trainer trainer = buildTrainerWithId(trainerUserId);
        Training training = buildTraining();
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);

        when(traineeRepository.findTraineeByUser_Username(createRequest.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainerRepository.findTrainerByUser_Username(createRequest.getTrainerUsername())).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(createRequest.getTrainingName())).thenReturn(Optional.of(buildFitnessTrainingType()));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);

        service.create(createRequest);

        verify(trainingRepository).save(captor.capture());
        Training captured = captor.getValue();
        assertEquals(traineeUserId, captured.getTrainee().getId());
        assertEquals(trainerUserId, captured.getTrainer().getId());
        verify(trainingMetrics).recordTrainingCreated();
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldReturnMappedResponses() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername(USERNAME)
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .trainerName("Mike")
                .trainingType("Fitness")
                .build();

        Trainee trainee = buildTrainee();
        Training training = buildTraining();
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(traineeRepository.findTraineeByUser_Username(filter.getTraineeUsername())).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        List<TrainingResponse> actual = service.getTraineeTrainingsByCriteria(filter);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
        verify(traineeRepository).findTraineeByUser_Username(filter.getTraineeUsername());
        verify(trainingRepository).findAll(any(Specification.class));
        verify(trainingMapper).toResponse(training);
        verify(trainingMetrics).recordTrainingRetrieval();
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldThrowException_WhenTraineeNotFound() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername("nonexistent")
                .build();

        when(traineeRepository.findTraineeByUser_Username("nonexistent")).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.getTraineeTrainingsByCriteria(filter));

        assertEquals("Trainee not found with username: nonexistent", exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username("nonexistent");
        verifyNoInteractions(trainingRepository);
        verify(trainingMetrics, never()).recordTrainingRetrieval();
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldReturnMappedResponses() {
        TrainerSearchFilter filter = TrainerSearchFilter.builder()
                .trainerUsername(TRAINER_USERNAME)
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .traineeName("John")
                .build();

        Trainer trainer = buildTrainer();
        Training training = buildTraining();
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainerRepository.findTrainerByUser_Username(filter.getTrainerUsername())).thenReturn(Optional.of(trainer));
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        List<TrainingResponse> actual = service.getTrainerTrainingsByCriteria(filter);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
        verify(trainerRepository).findTrainerByUser_Username(filter.getTrainerUsername());
        verify(trainingRepository).findAll(any(Specification.class));
        verify(trainingMapper).toResponse(training);
        verify(trainingMetrics).recordTrainingRetrieval();
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldThrowException_WhenTrainerNotFound() {
        TrainerSearchFilter filter = TrainerSearchFilter.builder()
                .trainerUsername("not.found")
                .build();

        when(trainerRepository.findTrainerByUser_Username("not.found")).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.getTrainerTrainingsByCriteria(filter));

        assertEquals("Trainer not found with username: not.found", exception.getMessage());
        verify(trainerRepository).findTrainerByUser_Username("not.found");
        verifyNoInteractions(trainingRepository);
        verify(trainingMetrics, never()).recordTrainingRetrieval();
    }

    private Training buildTraining() {
        return Training.builder()
                .id(TRAINING_ID)
                .trainee(buildTrainee())
                .trainer(buildTrainer())
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

    private Trainee buildTraineeWithId(Long id) {
        return buildTrainee().toBuilder()
                .id(id)
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

    private Trainer buildTrainerWithId(Long id) {
        return buildTrainer().toBuilder()
                .id(id)
                .build();
    }

    private TrainingType buildFitnessTrainingType() {
        return TrainingType.builder()
                .trainingTypeName(FITNESS_TYPE)
                .build();
    }
}
package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

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
    private TrainingDAO trainingDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private TrainingServiceImpl service;

    private final Training training = buildTraining();
    private final Trainee trainee = buildTrainee();
    private final Trainer trainer = buildTrainer();

    @Test
    void create_ShouldCreateTrainingSuccessfully() {
        TrainingCreateRequest createRequest = GymTestObjects.buildTrainingCreateRequest();
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(trainer));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);
        when(trainingDAO.create(training)).thenReturn(training);
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        TrainingResponse actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTraineeUsername(), actual.getTraineeUsername());
        assertEquals(expected.getTrainerUsername(), actual.getTrainerUsername());
        assertEquals(expected.getTrainingName(), actual.getTrainingName());
        assertEquals(expected.getTrainingDate(), actual.getTrainingDate());
        assertEquals(expected.getTrainingDuration(), actual.getTrainingDuration());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper).toEntity(createRequest);
        verify(trainingDAO).create(training);
        verify(trainingMapper).toResponse(training);

        assertEquals(trainee.getUserId(), training.getTraineeId());
        assertEquals(trainer.getUserId(), training.getTrainerId());
    }

    @Test
    void create_ShouldThrowExceptionWhenTraineeNotFound() {
        TrainingCreateRequest actual = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findById(actual.getTraineeId())).thenReturn(Optional.empty());
        when(trainerDAO.findById(actual.getTrainerId())).thenReturn(Optional.of(trainer));

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(actual));

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(actual.getTraineeId());
        verify(trainerDAO).findById(actual.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldThrowExceptionWhenTrainerNotFound() {
        TrainingCreateRequest actual = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findById(actual.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(actual.getTrainerId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.create(actual));

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(actual.getTraineeId());
        verify(trainerDAO).findById(actual.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldThrowExceptionWhenBothTraineeAndTrainerNotFound() {
        TrainingCreateRequest createRequest = GymTestObjects.buildTrainingCreateRequest();

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.empty());
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.create(createRequest));

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void findById_ShouldReturnTrainingWhenExists() {
        TrainingResponse expected = GymTestObjects.buildTrainingResponse();

        when(trainingDAO.findById(TRAINING_ID)).thenReturn(Optional.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(expected);

        Optional<TrainingResponse> actual = service.findById(TRAINING_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getTraineeUsername(), actual.get().getTraineeUsername());
        assertEquals(expected.getTrainerUsername(), actual.get().getTrainerUsername());
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
        TrainingCreateRequest createRequest = GymTestObjects.buildTrainingCreateRequest();
        TrainingResponse expectedResponse = GymTestObjects.buildTrainingResponse();

        trainee.setUserId(traineeUserId);
        trainer.setUserId(trainerUserId);

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(trainer));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);
        when(trainingDAO.create(training)).thenReturn(training);
        when(trainingMapper.toResponse(training)).thenReturn(expectedResponse);

        service.create(createRequest);

        assertEquals(traineeUserId, training.getTraineeId());
        assertEquals(trainerUserId, training.getTrainerId());

        verify(trainingDAO).create(training);
    }

    private Training buildTraining() {
        Training training = new Training();
        training.setTraineeId(TRAINEE_ID);
        training.setTrainerId(TRAINER_ID);
        training.setTrainingName(TRAINING_NAME);
        training.setTrainingDate(TRAINING_DATE);
        training.setDuration(TRAINING_DURATION);
        training.setTrainingType(new TrainingType(FITNESS_TYPE));

        return training;
    }

    private Trainee buildTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUserId(TRAINEE_ID);
        trainee.setFirstName(FIRST_NAME);
        trainee.setLastName(LAST_NAME);
        trainee.setUsername(USERNAME);
        trainee.setPassword("password123");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 5, 15));
        trainee.setAddress("123 Main St");

        return trainee;
    }

    private Trainer buildTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUserId(TRAINER_ID);
        trainer.setFirstName(TRAINER_FIRST_NAME);
        trainer.setLastName(TRAINER_LAST_NAME);
        trainer.setUsername(TRAINER_USERNAME);
        trainer.setPassword("password123");
        trainer.setIsActive(true);
        trainer.setSpecialization(new TrainingType(FITNESS_TYPE));

        return trainer;
    }
}
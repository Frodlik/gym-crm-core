package com.gym.crm.facade;

import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.FIRST_NAME;
import static com.gym.crm.facade.GymTestObjects.FITNESS_TYPE;
import static com.gym.crm.facade.GymTestObjects.LAST_NAME;
import static com.gym.crm.facade.GymTestObjects.TRAINEE_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINER_FIRST_NAME;
import static com.gym.crm.facade.GymTestObjects.TRAINER_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINER_LAST_NAME;
import static com.gym.crm.facade.GymTestObjects.TRAINER_USERNAME;
import static com.gym.crm.facade.GymTestObjects.TRAINING_DATE;
import static com.gym.crm.facade.GymTestObjects.TRAINING_DURATION;
import static com.gym.crm.facade.GymTestObjects.TRAINING_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINING_NAME;
import static com.gym.crm.facade.GymTestObjects.USERNAME;
import static com.gym.crm.facade.GymTestObjects.YOGA_TYPE;
import static com.gym.crm.facade.GymTestObjects.buildTraineeCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTraineeResponse;
import static com.gym.crm.facade.GymTestObjects.buildTraineeUpdateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerResponse;
import static com.gym.crm.facade.GymTestObjects.buildTrainerUpdateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @InjectMocks
    private GymFacade gymFacade;

    @Test
    void createTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeCreateRequest request = buildTraineeCreateRequest();
        TraineeResponse expectedResponse = buildTraineeResponse();

        when(traineeService.create(request)).thenReturn(expectedResponse);

        TraineeResponse result = gymFacade.createTrainee(request);

        assertNotNull(result);
        assertEquals(TRAINEE_ID, result.getId());
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(USERNAME, result.getUsername());
        verify(traineeService).create(request);
    }

    @Test
    void getTraineeById_ShouldCallServiceAndReturnResponse() {
        TraineeResponse expectedResponse = buildTraineeResponse();

        when(traineeService.findById(TRAINEE_ID)).thenReturn(Optional.of(expectedResponse));

        Optional<TraineeResponse> result = gymFacade.getTraineeById(TRAINEE_ID);

        assertTrue(result.isPresent());
        assertEquals(TRAINEE_ID, result.get().getId());
        assertEquals(USERNAME, result.get().getUsername());
        verify(traineeService).findById(TRAINEE_ID);
    }

    @Test
    void getTraineeById_ShouldReturnEmptyWhenNotFound() {
        Long nonExistentId = 999L;

        when(traineeService.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TraineeResponse> result = gymFacade.getTraineeById(nonExistentId);

        assertFalse(result.isPresent());
        verify(traineeService).findById(nonExistentId);
    }

    @Test
    void updateTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeUpdateRequest request = buildTraineeUpdateRequest();
        TraineeResponse updatedResponse = buildUpdatedTraineeResponse();

        when(traineeService.update(request)).thenReturn(updatedResponse);

        TraineeResponse result = gymFacade.updateTrainee(request);

        assertNotNull(result);
        assertEquals(TRAINEE_ID, result.getId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        verify(traineeService).update(request);
    }

    @Test
    void deleteTrainee_ShouldCallService() {
        gymFacade.deleteTrainee(TRAINEE_ID);

        verify(traineeService).delete(TRAINEE_ID);
    }

    @Test
    void createTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerCreateRequest request = buildTrainerCreateRequest();
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.create(request)).thenReturn(expectedResponse);

        TrainerResponse result = gymFacade.createTrainer(request);

        assertNotNull(result);
        assertEquals(TRAINER_ID, result.getId());
        assertEquals(TRAINER_FIRST_NAME, result.getFirstName());
        assertEquals(TRAINER_LAST_NAME, result.getLastName());
        assertEquals(TRAINER_USERNAME, result.getUsername());
        assertEquals(FITNESS_TYPE, result.getSpecialization().getTrainingTypeName());
        verify(trainerService).create(request);
    }

    @Test
    void getTrainerById_ShouldCallServiceAndReturnResponse() {
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.findById(TRAINER_ID)).thenReturn(Optional.of(expectedResponse));

        Optional<TrainerResponse> result = gymFacade.getTrainerById(TRAINER_ID);

        assertTrue(result.isPresent());
        assertEquals(TRAINER_ID, result.get().getId());
        assertEquals(TRAINER_USERNAME, result.get().getUsername());
        assertEquals(FITNESS_TYPE, result.get().getSpecialization().getTrainingTypeName());
        verify(trainerService).findById(TRAINER_ID);
    }

    @Test
    void getTrainerById_ShouldReturnEmptyWhenNotFound() {
        Long nonExistentId = 999L;

        when(trainerService.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = gymFacade.getTrainerById(nonExistentId);

        assertFalse(result.isPresent());
        verify(trainerService).findById(nonExistentId);
    }

    @Test
    void updateTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerUpdateRequest request = buildTrainerUpdateRequest();
        TrainerResponse updatedResponse = buildUpdatedTrainerResponse();

        when(trainerService.update(request)).thenReturn(updatedResponse);

        TrainerResponse result = gymFacade.updateTrainer(request);

        assertNotNull(result);
        assertEquals(TRAINER_ID, result.getId());
        assertEquals("Michael", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(YOGA_TYPE, result.getSpecialization().getTrainingTypeName());
        verify(trainerService).update(request);
    }

    @Test
    void createTraining_ShouldCallServiceAndReturnResponse() {
        TrainingCreateRequest request = buildTrainingCreateRequest();
        TrainingResponse expectedResponse = buildTrainingResponse();

        when(trainingService.create(request)).thenReturn(expectedResponse);

        TrainingResponse result = gymFacade.createTraining(request);

        assertNotNull(result);
        assertEquals(TRAINING_ID, result.getId());
        assertEquals(USERNAME, result.getTraineeUsername());
        assertEquals(TRAINER_USERNAME, result.getTrainerUsername());
        assertEquals(TRAINING_NAME, result.getTrainingName());
        assertEquals(TRAINING_DATE, result.getTrainingDate());
        assertEquals(TRAINING_DURATION, result.getTrainingDuration());
        verify(trainingService).create(request);
    }

    @Test
    void getTrainingById_ShouldCallServiceAndReturnResponse() {
        TrainingResponse expectedResponse = buildTrainingResponse();

        when(trainingService.findById(TRAINING_ID)).thenReturn(Optional.of(expectedResponse));

        Optional<TrainingResponse> result = gymFacade.getTrainingById(TRAINING_ID);

        assertTrue(result.isPresent());
        assertEquals(TRAINING_ID, result.get().getId());
        assertEquals(USERNAME, result.get().getTraineeUsername());
        assertEquals(TRAINER_USERNAME, result.get().getTrainerUsername());
        assertEquals(TRAINING_NAME, result.get().getTrainingName());
        verify(trainingService).findById(TRAINING_ID);
    }

    @Test
    void getTrainingById_ShouldReturnEmptyWhenNotFound() {
        Long nonExistentId = 999L;

        when(trainingService.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = gymFacade.getTrainingById(nonExistentId);

        assertFalse(result.isPresent());
        verify(trainingService).findById(nonExistentId);
    }

    @Test
    void constructor_ShouldInitializeServices() {
        TraineeService mockTraineeService = mock(TraineeService.class);
        TrainerService mockTrainerService = mock(TrainerService.class);
        TrainingService mockTrainingService = mock(TrainingService.class);

        when(mockTraineeService.findById(TRAINEE_ID)).thenReturn(Optional.of(buildTraineeResponse()));

        GymFacade facade = new GymFacade(mockTraineeService, mockTrainerService, mockTrainingService);

        assertNotNull(facade);

        Optional<TraineeResponse> result = facade.getTraineeById(TRAINEE_ID);

        assertTrue(result.isPresent());
        verify(mockTraineeService).findById(TRAINEE_ID);
    }

    private TraineeResponse buildUpdatedTraineeResponse() {
        TraineeResponse response = new TraineeResponse();
        response.setId(TRAINEE_ID);
        response.setFirstName("Jane");
        response.setLastName("Smith");

        return response;
    }

    private TrainerResponse buildUpdatedTrainerResponse() {
        TrainerResponse response = new TrainerResponse();
        response.setId(TRAINER_ID);
        response.setFirstName("Michael");
        response.setLastName("Smith");
        response.setSpecialization(new TrainingType(YOGA_TYPE));

        return response;
    }
}
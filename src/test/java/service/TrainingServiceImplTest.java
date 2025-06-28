package service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock
    private TrainingDAO trainingDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;
    private Trainee trainee;
    private Trainer trainer;
    private TrainingCreateRequest createRequest;
    private TrainingResponse trainingResponse;

    @BeforeEach
    void setUp() {
        TrainingType trainingType = new TrainingType("Fitness");

        trainee = new Trainee();
        trainee.setUserId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("john.doe");

        trainer = new Trainer();
        trainer.setUserId(2L);
        trainer.setFirstName("Mike");
        trainer.setLastName("Johnson");
        trainer.setUsername("mike.johnson");

        training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setTrainingName("Morning Workout");
        training.setTrainingType(trainingType);
        training.setTrainingDate(LocalDate.of(2024, 1, 15));
        training.setDuration(60);

        createRequest = new TrainingCreateRequest();
        createRequest.setTraineeId(1L);
        createRequest.setTrainerId(2L);
        createRequest.setTrainingName("Morning Workout");
        createRequest.setTrainingDate(LocalDate.of(2024, 1, 15));
        createRequest.setTrainingDuration(60);

        trainingResponse = new TrainingResponse();
        trainingResponse.setId(1L);
        trainingResponse.setTraineeUsername("john.doe");
        trainingResponse.setTrainerUsername("mike.johnson");
        trainingResponse.setTrainingName("Morning Workout");
        trainingResponse.setTrainingType(trainingType);
        trainingResponse.setTrainingDate(LocalDate.of(2024, 1, 15));
        trainingResponse.setTrainingDuration(60);
    }

    @Test
    void create_ShouldCreateTrainingSuccessfully() {
        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(trainer));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);
        when(trainingDAO.create(training)).thenReturn(training);
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);

        TrainingResponse result = trainingService.create(createRequest);

        assertNotNull(result);
        assertEquals(trainingResponse.getId(), result.getId());
        assertEquals(trainingResponse.getTraineeUsername(), result.getTraineeUsername());
        assertEquals(trainingResponse.getTrainerUsername(), result.getTrainerUsername());
        assertEquals(trainingResponse.getTrainingName(), result.getTrainingName());
        assertEquals(trainingResponse.getTrainingDate(), result.getTrainingDate());
        assertEquals(trainingResponse.getTrainingDuration(), result.getTrainingDuration());

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
        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.empty());
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(trainer));

        CoreServiceException exception = assertThrows(
                CoreServiceException.class,
                () -> trainingService.create(createRequest)
        );

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldThrowExceptionWhenTrainerNotFound() {
        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(
                CoreServiceException.class,
                () -> trainingService.create(createRequest)
        );

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldThrowExceptionWhenBothTraineeAndTrainerNotFound() {
        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.empty());
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(
                CoreServiceException.class,
                () -> trainingService.create(createRequest)
        );

        assertEquals("Trainee or/and Trainer was not found", exception.getMessage());

        verify(traineeDAO).findById(createRequest.getTraineeId());
        verify(trainerDAO).findById(createRequest.getTrainerId());
        verify(trainingMapper, never()).toEntity(any());
        verify(trainingDAO, never()).create(any());
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void findById_ShouldReturnTrainingWhenExists() {
        Long trainingId = 1L;
        when(trainingDAO.findById(trainingId)).thenReturn(Optional.of(training));
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);

        Optional<TrainingResponse> result = trainingService.findById(trainingId);

        assertTrue(result.isPresent());
        assertEquals(trainingResponse.getId(), result.get().getId());
        assertEquals(trainingResponse.getTraineeUsername(), result.get().getTraineeUsername());
        assertEquals(trainingResponse.getTrainerUsername(), result.get().getTrainerUsername());
        assertEquals(trainingResponse.getTrainingName(), result.get().getTrainingName());

        verify(trainingDAO).findById(trainingId);
        verify(trainingMapper).toResponse(training);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainingId = 999L;
        when(trainingDAO.findById(trainingId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = trainingService.findById(trainingId);

        assertFalse(result.isPresent());

        verify(trainingDAO).findById(trainingId);
        verify(trainingMapper, never()).toResponse(any());
    }

    @Test
    void create_ShouldSetCorrectUserIds() {
        Long traineeUserId = 100L;
        Long trainerUserId = 200L;

        trainee.setUserId(traineeUserId);
        trainer.setUserId(trainerUserId);

        when(traineeDAO.findById(createRequest.getTraineeId())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findById(createRequest.getTrainerId())).thenReturn(Optional.of(trainer));
        when(trainingMapper.toEntity(createRequest)).thenReturn(training);
        when(trainingDAO.create(training)).thenReturn(training);
        when(trainingMapper.toResponse(training)).thenReturn(trainingResponse);

        trainingService.create(createRequest);

        assertEquals(traineeUserId, training.getTraineeId());
        assertEquals(trainerUserId, training.getTrainerId());

        verify(trainingDAO).create(training);
    }
}
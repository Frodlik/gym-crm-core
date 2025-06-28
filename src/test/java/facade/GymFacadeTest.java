package facade;

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

    private TraineeCreateRequest traineeCreateRequest;
    private TraineeUpdateRequest traineeUpdateRequest;
    private TraineeResponse traineeResponse;

    private TrainerCreateRequest trainerCreateRequest;
    private TrainerUpdateRequest trainerUpdateRequest;
    private TrainerResponse trainerResponse;

    private TrainingCreateRequest trainingCreateRequest;
    private TrainingResponse trainingResponse;

    @BeforeEach
    void setUp() {
        traineeCreateRequest = new TraineeCreateRequest();
        traineeCreateRequest.setFirstName("John");
        traineeCreateRequest.setLastName("Doe");
        traineeCreateRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeCreateRequest.setAddress("123 Main St");

        traineeUpdateRequest = new TraineeUpdateRequest();
        traineeUpdateRequest.setId(1L);
        traineeUpdateRequest.setFirstName("Jane");
        traineeUpdateRequest.setLastName("Smith");
        traineeUpdateRequest.setIsActive(false);
        traineeUpdateRequest.setDateOfBirth(LocalDate.of(1985, 5, 15));
        traineeUpdateRequest.setAddress("456 Oak Ave");

        traineeResponse = new TraineeResponse();
        traineeResponse.setId(1L);
        traineeResponse.setFirstName("John");
        traineeResponse.setLastName("Doe");
        traineeResponse.setUsername("john.doe");
        traineeResponse.setActive(true);
        traineeResponse.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeResponse.setAddress("123 Main St");

        trainerCreateRequest = new TrainerCreateRequest();
        trainerCreateRequest.setFirstName("Mike");
        trainerCreateRequest.setLastName("Johnson");
        trainerCreateRequest.setSpecialization(new TrainingType("FITNESS"));

        trainerUpdateRequest = new TrainerUpdateRequest();
        trainerUpdateRequest.setId(2L);
        trainerUpdateRequest.setFirstName("Michael");
        trainerUpdateRequest.setLastName("Smith");
        trainerUpdateRequest.setIsActive(false);
        trainerUpdateRequest.setSpecialization(new TrainingType("YOGA"));

        trainerResponse = new TrainerResponse();
        trainerResponse.setId(2L);
        trainerResponse.setFirstName("Mike");
        trainerResponse.setLastName("Johnson");
        trainerResponse.setUsername("mike.johnson");
        trainerResponse.setActive(true);
        trainerResponse.setSpecialization(new TrainingType("FITNESS"));

        trainingCreateRequest = new TrainingCreateRequest();
        trainingCreateRequest.setTraineeId(1L);
        trainingCreateRequest.setTrainerId(2L);
        trainingCreateRequest.setTrainingName("Morning Workout");
        trainingCreateRequest.setTrainingDate(LocalDate.of(2024, 1, 15));
        trainingCreateRequest.setTrainingDuration(60);

        trainingResponse = new TrainingResponse();
        trainingResponse.setId(3L);
        trainingResponse.setTraineeUsername("john.doe");
        trainingResponse.setTrainerUsername("mike.johnson");
        trainingResponse.setTrainingName("Morning Workout");
        trainingResponse.setTrainingType(new TrainingType("FITNESS"));
        trainingResponse.setTrainingDate(LocalDate.of(2024, 1, 15));
        trainingResponse.setTrainingDuration(60);
    }

    @Test
    void createTrainee_ShouldCallServiceAndReturnResponse() {
        when(traineeService.create(traineeCreateRequest)).thenReturn(traineeResponse);

        TraineeResponse result = gymFacade.createTrainee(traineeCreateRequest);

        assertNotNull(result);
        assertEquals(traineeResponse.getId(), result.getId());
        assertEquals(traineeResponse.getFirstName(), result.getFirstName());
        assertEquals(traineeResponse.getLastName(), result.getLastName());
        assertEquals(traineeResponse.getUsername(), result.getUsername());

        verify(traineeService).create(traineeCreateRequest);
    }

    @Test
    void getTraineeById_ShouldCallServiceAndReturnResponse() {
        Long traineeId = 1L;
        when(traineeService.findById(traineeId)).thenReturn(Optional.of(traineeResponse));

        Optional<TraineeResponse> result = gymFacade.getTraineeById(traineeId);

        assertTrue(result.isPresent());
        assertEquals(traineeResponse.getId(), result.get().getId());
        assertEquals(traineeResponse.getUsername(), result.get().getUsername());

        verify(traineeService).findById(traineeId);
    }

    @Test
    void getTraineeById_ShouldReturnEmptyWhenNotFound() {
        Long traineeId = 999L;
        when(traineeService.findById(traineeId)).thenReturn(Optional.empty());

        Optional<TraineeResponse> result = gymFacade.getTraineeById(traineeId);

        assertFalse(result.isPresent());

        verify(traineeService).findById(traineeId);
    }

    @Test
    void updateTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeResponse updatedResponse = new TraineeResponse();
        updatedResponse.setId(1L);
        updatedResponse.setFirstName("Jane");
        updatedResponse.setLastName("Smith");

        when(traineeService.update(traineeUpdateRequest)).thenReturn(updatedResponse);

        TraineeResponse result = gymFacade.updateTrainee(traineeUpdateRequest);

        assertNotNull(result);
        assertEquals(updatedResponse.getId(), result.getId());
        assertEquals(updatedResponse.getFirstName(), result.getFirstName());
        assertEquals(updatedResponse.getLastName(), result.getLastName());

        verify(traineeService).update(traineeUpdateRequest);
    }

    @Test
    void deleteTrainee_ShouldCallService() {
        Long traineeId = 1L;

        gymFacade.deleteTrainee(traineeId);

        verify(traineeService).delete(traineeId);
    }

    @Test
    void createTrainer_ShouldCallServiceAndReturnResponse() {
        when(trainerService.create(trainerCreateRequest)).thenReturn(trainerResponse);

        TrainerResponse result = gymFacade.createTrainer(trainerCreateRequest);

        assertNotNull(result);
        assertEquals(trainerResponse.getId(), result.getId());
        assertEquals(trainerResponse.getFirstName(), result.getFirstName());
        assertEquals(trainerResponse.getLastName(), result.getLastName());
        assertEquals(trainerResponse.getUsername(), result.getUsername());
        assertEquals(trainerResponse.getSpecialization(), result.getSpecialization());

        verify(trainerService).create(trainerCreateRequest);
    }

    @Test
    void getTrainerById_ShouldCallServiceAndReturnResponse() {
        Long trainerId = 2L;
        when(trainerService.findById(trainerId)).thenReturn(Optional.of(trainerResponse));

        Optional<TrainerResponse> result = gymFacade.getTrainerById(trainerId);

        assertTrue(result.isPresent());
        assertEquals(trainerResponse.getId(), result.get().getId());
        assertEquals(trainerResponse.getUsername(), result.get().getUsername());
        assertEquals(trainerResponse.getSpecialization(), result.get().getSpecialization());

        verify(trainerService).findById(trainerId);
    }

    @Test
    void getTrainerById_ShouldReturnEmptyWhenNotFound() {
        Long trainerId = 999L;
        when(trainerService.findById(trainerId)).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = gymFacade.getTrainerById(trainerId);

        assertFalse(result.isPresent());

        verify(trainerService).findById(trainerId);
    }

    @Test
    void updateTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerResponse updatedResponse = new TrainerResponse();
        updatedResponse.setId(2L);
        updatedResponse.setFirstName("Michael");
        updatedResponse.setLastName("Smith");
        updatedResponse.setSpecialization(new TrainingType("YOGA"));

        when(trainerService.update(trainerUpdateRequest)).thenReturn(updatedResponse);

        TrainerResponse result = gymFacade.updateTrainer(trainerUpdateRequest);

        assertNotNull(result);
        assertEquals(updatedResponse.getId(), result.getId());
        assertEquals(updatedResponse.getFirstName(), result.getFirstName());
        assertEquals(updatedResponse.getLastName(), result.getLastName());
        assertEquals(updatedResponse.getSpecialization(), result.getSpecialization());

        verify(trainerService).update(trainerUpdateRequest);
    }

    @Test
    void createTraining_ShouldCallServiceAndReturnResponse() {
        when(trainingService.create(trainingCreateRequest)).thenReturn(trainingResponse);

        TrainingResponse result = gymFacade.createTraining(trainingCreateRequest);

        assertNotNull(result);
        assertEquals(trainingResponse.getId(), result.getId());
        assertEquals(trainingResponse.getTraineeUsername(), result.getTraineeUsername());
        assertEquals(trainingResponse.getTrainerUsername(), result.getTrainerUsername());
        assertEquals(trainingResponse.getTrainingName(), result.getTrainingName());
        assertEquals(trainingResponse.getTrainingDate(), result.getTrainingDate());
        assertEquals(trainingResponse.getTrainingDuration(), result.getTrainingDuration());

        verify(trainingService).create(trainingCreateRequest);
    }

    @Test
    void getTrainingById_ShouldCallServiceAndReturnResponse() {
        Long trainingId = 3L;
        when(trainingService.findById(trainingId)).thenReturn(Optional.of(trainingResponse));

        Optional<TrainingResponse> result = gymFacade.getTrainingById(trainingId);

        assertTrue(result.isPresent());
        assertEquals(trainingResponse.getId(), result.get().getId());
        assertEquals(trainingResponse.getTraineeUsername(), result.get().getTraineeUsername());
        assertEquals(trainingResponse.getTrainerUsername(), result.get().getTrainerUsername());
        assertEquals(trainingResponse.getTrainingName(), result.get().getTrainingName());

        verify(trainingService).findById(trainingId);
    }

    @Test
    void getTrainingById_ShouldReturnEmptyWhenNotFound() {
        Long trainingId = 999L;
        when(trainingService.findById(trainingId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> result = gymFacade.getTrainingById(trainingId);

        assertFalse(result.isPresent());

        verify(trainingService).findById(trainingId);
    }

    @Test
    void constructor_ShouldInitializeServices() {
        TraineeService mockTraineeService = mock(TraineeService.class);
        TrainerService mockTrainerService = mock(TrainerService.class);
        TrainingService mockTrainingService = mock(TrainingService.class);

        GymFacade facade = new GymFacade(mockTraineeService, mockTrainerService, mockTrainingService);

        assertNotNull(facade);
        when(mockTraineeService.findById(1L)).thenReturn(Optional.of(traineeResponse));

        Optional<TraineeResponse> result = facade.getTraineeById(1L);

        assertTrue(result.isPresent());
        verify(mockTraineeService).findById(1L);
    }
}
package service;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.impl.TrainerServiceImpl;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
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
class TrainerServiceImplTest {
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TrainerMapper trainerMapper;
    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private TrainerCreateRequest createRequest;
    private TrainerUpdateRequest updateRequest;
    private TrainerResponse trainerResponse;

    @BeforeEach
    void setUp() {
        trainer = new Trainer();
        trainer.setUserId(1L);
        trainer.setFirstName("Mike");
        trainer.setLastName("Johnson");
        trainer.setUsername("mike.johnson");
        trainer.setPassword("password123");
        trainer.setIsActive(true);
        trainer.setSpecialization(new TrainingType("Fitness"));

        createRequest = new TrainerCreateRequest();
        createRequest.setFirstName("Mike");
        createRequest.setLastName("Johnson");
        createRequest.setSpecialization(new TrainingType("Fitness"));

        updateRequest = new TrainerUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setFirstName("Michael");
        updateRequest.setLastName("Smith");
        updateRequest.setIsActive(false);
        updateRequest.setSpecialization(new TrainingType("Yoga"));

        trainerResponse = new TrainerResponse();
        trainerResponse.setId(1L);
        trainerResponse.setFirstName("Mike");
        trainerResponse.setLastName("Johnson");
        trainerResponse.setUsername("mike.johnson");
        trainerResponse.setActive(true);
        trainerResponse.setSpecialization(new TrainingType("Fitness"));
    }

    @Test
    void create_ShouldCreateTrainerSuccessfully() {
        List<Trainer> existingTrainers = Arrays.asList(
                createTrainerWithUsername("existing.trainer1"),
                createTrainerWithUsername("existing.trainer2")
        );
        List<String> existingUsernames = Arrays.asList("existing.trainer1", "existing.trainer2");

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername("Mike", "Johnson", existingUsernames))
                .thenReturn("mike.johnson");
        when(userCredentialsGenerator.generatePassword()).thenReturn("generatedPassword");
        when(trainerDAO.create(trainer)).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        TrainerResponse result = trainerService.create(createRequest);

        assertNotNull(result);
        assertEquals(trainerResponse.getId(), result.getId());
        assertEquals(trainerResponse.getUsername(), result.getUsername());
        assertEquals(trainerResponse.getSpecialization(), result.getSpecialization());

        verify(trainerMapper).toEntity(createRequest);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername("Mike", "Johnson", existingUsernames);
        verify(userCredentialsGenerator).generatePassword();
        verify(trainerDAO).create(trainer);
        verify(trainerMapper).toResponse(trainer);

        assertEquals("mike.johnson", trainer.getUsername());
        assertEquals("generatedPassword", trainer.getPassword());
    }

    @Test
    void create_ShouldHandleEmptyExistingUsernames() {
        List<Trainer> existingTrainers = List.of();
        List<String> existingUsernames = List.of();

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername("Mike", "Johnson", existingUsernames))
                .thenReturn("mike.johnson");
        when(userCredentialsGenerator.generatePassword()).thenReturn("generatedPassword");
        when(trainerDAO.create(trainer)).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        TrainerResponse result = trainerService.create(createRequest);

        assertNotNull(result);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername("Mike", "Johnson", existingUsernames);
    }

    @Test
    void findById_ShouldReturnTrainerWhenExists() {
        Long trainerId = 1L;
        when(trainerDAO.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        Optional<TrainerResponse> result = trainerService.findById(trainerId);

        assertTrue(result.isPresent());
        assertEquals(trainerResponse.getId(), result.get().getId());
        assertEquals(trainerResponse.getUsername(), result.get().getUsername());
        assertEquals(trainerResponse.getSpecialization(), result.get().getSpecialization());

        verify(trainerDAO).findById(trainerId);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainerId = 999L;
        when(trainerDAO.findById(trainerId)).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = trainerService.findById(trainerId);

        assertFalse(result.isPresent());

        verify(trainerDAO).findById(trainerId);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTrainerSuccessfully() {
        Trainer updatedTrainer = new Trainer();
        updatedTrainer.setUserId(1L);
        updatedTrainer.setFirstName("Michael");
        updatedTrainer.setLastName("Smith");
        updatedTrainer.setIsActive(false);
        updatedTrainer.setSpecialization(new TrainingType("Yoga"));

        TrainerResponse updatedResponse = new TrainerResponse();
        updatedResponse.setId(1L);
        updatedResponse.setFirstName("Michael");
        updatedResponse.setLastName("Smith");
        updatedResponse.setActive(false);
        updatedResponse.setSpecialization(new TrainingType("Yoga"));

        when(trainerDAO.findById(updateRequest.getId())).thenReturn(Optional.of(trainer));
        when(trainerDAO.update(trainer)).thenReturn(updatedTrainer);
        when(trainerMapper.toResponse(updatedTrainer)).thenReturn(updatedResponse);

        TrainerResponse result = trainerService.update(updateRequest);

        assertNotNull(result);
        assertEquals(updatedResponse.getId(), result.getId());
        assertEquals(updatedResponse.getFirstName(), result.getFirstName());
        assertEquals(updatedResponse.getLastName(), result.getLastName());
        assertEquals(updatedResponse.isActive(), result.isActive());
        assertEquals(updatedResponse.getSpecialization(), result.getSpecialization());

        verify(trainerDAO).findById(updateRequest.getId());
        verify(trainerDAO).update(trainer);
        verify(trainerMapper).toResponse(updatedTrainer);

        assertEquals("Michael", trainer.getFirstName());
        assertEquals("Smith", trainer.getLastName());
        assertEquals(false, trainer.getIsActive());
        assertEquals("Yoga", trainer.getSpecialization().getTrainingTypeName());
    }

    @Test
    void update_ShouldThrowExceptionWhenTrainerNotFound() {
        when(trainerDAO.findById(updateRequest.getId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(
                CoreServiceException.class,
                () -> trainerService.update(updateRequest)
        );

        assertEquals("Trainer not found with id: " + updateRequest.getId(), exception.getMessage());

        verify(trainerDAO).findById(updateRequest.getId());
        verify(trainerDAO, never()).update(any());
        verify(trainerMapper, never()).toResponse(any());
    }

    private Trainer createTrainerWithUsername(String username) {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        return trainer;
    }
}

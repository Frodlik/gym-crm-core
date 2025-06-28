package service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.service.impl.TraineeServiceImpl;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TraineeMapper traineeMapper;
    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private TraineeCreateRequest createRequest;
    private TraineeUpdateRequest updateRequest;
    private TraineeResponse traineeResponse;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setUserId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("john.doe");
        trainee.setPassword("password123");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");

        createRequest = new TraineeCreateRequest();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        createRequest.setAddress("123 Main St");

        updateRequest = new TraineeUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setIsActive(false);
        updateRequest.setDateOfBirth(LocalDate.of(1985, 5, 15));
        updateRequest.setAddress("456 Oak Ave");

        traineeResponse = new TraineeResponse();
        traineeResponse.setId(1L);
        traineeResponse.setFirstName("John");
        traineeResponse.setLastName("Doe");
        traineeResponse.setUsername("john.doe");
        traineeResponse.setActive(true);
        traineeResponse.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeResponse.setAddress("123 Main St");
    }

    @Test
    void create_ShouldCreateTraineeSuccessfully() {
        List<Trainee> existingTrainees = Arrays.asList(
                createTraineeWithUsername("existing.user1"),
                createTraineeWithUsername("existing.user2")
        );
        List<String> existingUsernames = Arrays.asList("existing.user1", "existing.user2");

        when(traineeMapper.toEntity(createRequest)).thenReturn(trainee);
        when(traineeDAO.findAll()).thenReturn(existingTrainees);
        when(userCredentialsGenerator.generateUsername("John", "Doe", existingUsernames))
                .thenReturn("john.doe");
        when(userCredentialsGenerator.generatePassword()).thenReturn("generatedPassword");
        when(traineeDAO.create(trainee)).thenReturn(trainee);
        when(traineeMapper.toResponse(trainee)).thenReturn(traineeResponse);

        TraineeResponse result = traineeService.create(createRequest);

        assertNotNull(result);
        assertEquals(traineeResponse.getId(), result.getId());
        assertEquals(traineeResponse.getUsername(), result.getUsername());

        verify(traineeMapper).toEntity(createRequest);
        verify(traineeDAO).findAll();
        verify(userCredentialsGenerator).generateUsername("John", "Doe", existingUsernames);
        verify(userCredentialsGenerator).generatePassword();
        verify(traineeDAO).create(trainee);
        verify(traineeMapper).toResponse(trainee);

        assertEquals("generatedPassword", trainee.getPassword());
        assertEquals("john.doe", trainee.getUsername());
    }

    @Test
    void findById_ShouldReturnTraineeWhenExists() {
        Long traineeId = 1L;
        when(traineeDAO.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(traineeResponse);

        Optional<TraineeResponse> result = traineeService.findById(traineeId);

        assertTrue(result.isPresent());
        assertEquals(traineeResponse.getId(), result.get().getId());
        assertEquals(traineeResponse.getUsername(), result.get().getUsername());

        verify(traineeDAO).findById(traineeId);
        verify(traineeMapper).toResponse(trainee);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long traineeId = 999L;
        when(traineeDAO.findById(traineeId)).thenReturn(Optional.empty());

        Optional<TraineeResponse> result = traineeService.findById(traineeId);

        assertFalse(result.isPresent());

        verify(traineeDAO).findById(traineeId);
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTraineeSuccessfully() {
        Trainee updatedTrainee = new Trainee();
        updatedTrainee.setUserId(1L);
        updatedTrainee.setFirstName("Jane");
        updatedTrainee.setLastName("Smith");
        updatedTrainee.setIsActive(false);
        updatedTrainee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        updatedTrainee.setAddress("456 Oak Ave");

        TraineeResponse updatedResponse = new TraineeResponse();
        updatedResponse.setId(1L);
        updatedResponse.setFirstName("Jane");
        updatedResponse.setLastName("Smith");
        updatedResponse.setActive(false);

        when(traineeDAO.findById(updateRequest.getId())).thenReturn(Optional.of(trainee));
        when(traineeDAO.update(trainee)).thenReturn(updatedTrainee);
        when(traineeMapper.toResponse(updatedTrainee)).thenReturn(updatedResponse);

        TraineeResponse result = traineeService.update(updateRequest);

        assertNotNull(result);
        assertEquals(updatedResponse.getId(), result.getId());
        assertEquals(updatedResponse.getFirstName(), result.getFirstName());
        assertEquals(updatedResponse.getLastName(), result.getLastName());
        assertEquals(updatedResponse.isActive(), result.isActive());

        verify(traineeDAO).findById(updateRequest.getId());
        verify(traineeDAO).update(trainee);
        verify(traineeMapper).toResponse(updatedTrainee);

        assertEquals("Jane", trainee.getFirstName());
        assertEquals("Smith", trainee.getLastName());
        assertEquals(false, trainee.getIsActive());
        assertEquals(LocalDate.of(1985, 5, 15), trainee.getDateOfBirth());
        assertEquals("456 Oak Ave", trainee.getAddress());
    }

    @Test
    void update_ShouldThrowExceptionWhenTraineeNotFound() {
        when(traineeDAO.findById(updateRequest.getId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(
                CoreServiceException.class,
                () -> traineeService.update(updateRequest)
        );

        assertEquals("Trainee not found with id: " + updateRequest.getId(), exception.getMessage());

        verify(traineeDAO).findById(updateRequest.getId());
        verify(traineeDAO, never()).update(any());
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void delete_ShouldCallDAODelete() {
        Long traineeId = 1L;

        traineeService.delete(traineeId);

        verify(traineeDAO).delete(traineeId);
    }

    private Trainee createTraineeWithUsername(String username) {
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        return trainee;
    }
}

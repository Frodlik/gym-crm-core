package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.buildTraineeResponse;
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
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final Long TRAINEE_ID = 1L;
    private static final String GENERATED_PASSWORD = "generatedPassword";

    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TraineeMapper traineeMapper;
    @InjectMocks
    private TraineeServiceImpl traineeService;

    private final Trainee trainee = buildTrainee();

    @Test
    void create_ShouldCreateTraineeSuccessfully() {
        TraineeCreateRequest createRequest = GymTestObjects.buildTraineeCreateRequest();
        List<Trainee> existingTrainees = Arrays.asList(
                createTraineeWithUsername("existing.user1"),
                createTraineeWithUsername("existing.user2")
        );
        List<String> existingUsernames = Arrays.asList("existing.user1", "existing.user2");
        TraineeResponse expectedResponse = buildTraineeResponse();

        when(traineeMapper.toEntity(createRequest)).thenReturn(trainee);
        when(traineeDAO.findAll()).thenReturn(existingTrainees);
        when(userCredentialsGenerator.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames))
                .thenReturn(USERNAME);
        when(userCredentialsGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(traineeDAO.create(trainee)).thenReturn(trainee);
        when(traineeMapper.toResponse(trainee)).thenReturn(expectedResponse);

        TraineeResponse result = traineeService.create(createRequest);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getUsername(), result.getUsername());

        verify(traineeMapper).toEntity(createRequest);
        verify(traineeDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generatePassword();
        verify(traineeDAO).create(trainee);
        verify(traineeMapper).toResponse(trainee);

        assertEquals(GENERATED_PASSWORD, trainee.getPassword());
        assertEquals(USERNAME, trainee.getUsername());
    }

    @Test
    void findById_ShouldReturnTraineeWhenExists() {
        TraineeResponse expectedResponse = GymTestObjects.buildTraineeResponse();

        when(traineeDAO.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expectedResponse);

        Optional<TraineeResponse> result = traineeService.findById(TRAINEE_ID);

        assertTrue(result.isPresent());
        assertEquals(expectedResponse.getId(), result.get().getId());
        assertEquals(expectedResponse.getUsername(), result.get().getUsername());

        verify(traineeDAO).findById(TRAINEE_ID);
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
        TraineeUpdateRequest updateRequest = GymTestObjects.buildTraineeUpdateRequest();
        Trainee updatedTrainee = buildUpdatedTrainee();
        TraineeResponse updatedResponse = buildUpdatedResponse();

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
        TraineeUpdateRequest updateRequest = GymTestObjects.buildTraineeUpdateRequest();

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
        traineeService.delete(TRAINEE_ID);

        verify(traineeDAO).delete(TRAINEE_ID);
    }

    private Trainee buildTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUserId(TRAINEE_ID);
        trainee.setFirstName(FIRST_NAME);
        trainee.setLastName(LAST_NAME);
        trainee.setUsername(USERNAME);
        trainee.setPassword(PASSWORD);
        trainee.setIsActive(true);
        trainee.setDateOfBirth(BIRTH_DATE);
        trainee.setAddress(ADDRESS);

        return trainee;
    }

    private Trainee buildUpdatedTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUserId(TRAINEE_ID);
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setIsActive(false);
        trainee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        trainee.setAddress("456 Oak Ave");

        return trainee;
    }

    private TraineeResponse buildUpdatedResponse() {
        TraineeResponse response = new TraineeResponse();
        response.setId(TRAINEE_ID);
        response.setFirstName("Jane");
        response.setLastName("Smith");
        response.setActive(false);

        return response;
    }

    private Trainee createTraineeWithUsername(String username) {
        Trainee trainee = new Trainee();
        trainee.setUsername(username);

        return trainee;
    }
}

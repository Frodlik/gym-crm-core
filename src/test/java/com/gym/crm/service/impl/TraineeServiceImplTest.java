package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.model.TrainerModel;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String RAW_PASSWORD = "rawPassword123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final Long TRAINEE_ID = 1L;

    private final Trainee trainee = buildTrainee();
    @Captor
    private ArgumentCaptor<Trainee> captor;

    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TraineeMapper traineeMapper;
    @InjectMocks
    private TraineeServiceImpl service;

    @Test
    void create_ShouldCreateTraineeSuccessfully() {
        TraineeCreateRequestDto createRequest = buildTraineeCreateRequest();
        List<Trainee> existingTrainees = List.of(
                createTraineeWithUsername("existing.user1"),
                createTraineeWithUsername("existing.user2")
        );
        List<String> existingUsernames = List.of("existing.user1", "existing.user2");
        TraineeCreateResponseDto expected = TraineeCreateResponseDto.builder()
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(traineeMapper.toEntity(createRequest)).thenReturn(trainee);
        when(traineeDAO.findAll()).thenReturn(existingTrainees);
        when(userCredentialsGenerator.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames))
                .thenReturn(USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(traineeDAO.create(any(Trainee.class))).thenReturn(trainee);

        TraineeCreateResponseDto actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
        verify(traineeMapper).toEntity(createRequest);
        verify(traineeDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generateRawPassword();
        verify(userCredentialsGenerator).encodePassword(RAW_PASSWORD);
        verify(traineeDAO).create(any(Trainee.class));
        verify(traineeDAO).create(captor.capture());

        Trainee captured = captor.getValue();
        assertEquals(ENCODED_PASSWORD, captured.getUser().getPassword());
        assertEquals(USERNAME, captured.getUser().getUsername());
        assertEquals(FIRST_NAME, captured.getUser().getFirstName());
        assertEquals(LAST_NAME, captured.getUser().getLastName());
        assertTrue(captured.getUser().getIsActive());
    }

    @Test
    void findById_ShouldReturnTraineeWhenExists() {
        TraineeGetResponseDto expected = buildTraineeGetResponse();

        when(traineeDAO.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        Optional<TraineeGetResponseDto> actual = service.findById(TRAINEE_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getUsername(), actual.get().getUsername());
        verify(traineeDAO).findById(TRAINEE_ID);
        verify(traineeMapper).toResponse(trainee);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long traineeId = 999L;

        when(traineeDAO.findById(traineeId)).thenReturn(Optional.empty());

        Optional<TraineeGetResponseDto> actual = service.findById(traineeId);

        assertFalse(actual.isPresent());
        verify(traineeDAO).findById(traineeId);
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void findByUsername_ShouldReturnTraineeWhenExists() {
        Trainee buildTrainee = buildTrainee();
        TraineeGetResponseDto expected = buildTraineeGetResponse();

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee));
        when(traineeMapper.toResponse(buildTrainee)).thenReturn(expected);

        TraineeGetResponseDto actual = service.findByUsername(USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeMapper).toResponse(buildTrainee);
    }

    @Test
    void findByUsername_ShouldThrowExceptionWhenNotExists() {
        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.findByUsername(USERNAME));

        assertEquals("Unable to find trainee with username: " + USERNAME, exception.getMessage());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTraineeSuccessfully() {
        TraineeUpdateRequestDto updateRequest = buildTraineeUpdateRequest();
        Trainee updatedTrainee = buildUpdatedTrainee();
        TraineeUpdateResponseDto expected = buildTraineeUpdateResponse();

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDAO.update(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toUpdateResponseDto(any(Trainee.class))).thenReturn(expected);

        TraineeUpdateResponseDto actual = service.update(updateRequest, USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.isActive(), actual.isActive());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO).update(any(Trainee.class));
        verify(traineeMapper).toUpdateResponseDto(updatedTrainee);
        verify(traineeDAO).update(captor.capture());

        Trainee captured = captor.getValue();
        assertEquals("Jane", captured.getUser().getFirstName());
        assertEquals("Smith", captured.getUser().getLastName());
        assertFalse(captured.getUser().getIsActive());
        assertEquals(LocalDate.of(1985, 5, 15), captured.getDateOfBirth());
        assertEquals("456 Oak Ave", captured.getAddress());
    }

    @Test
    void update_ShouldThrowExceptionWhenTraineeNotFound() {
        TraineeUpdateRequestDto updateRequest = buildTraineeUpdateRequest();

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.update(updateRequest, USERNAME));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO, never()).update(any());
        verify(traineeMapper, never()).toUpdateResponseDto(any());
    }

    @Test
    void changePassword_ShouldUpdatePasswordWhenOldPasswordMatches() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword("newSecurePassword");

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, trainee.getUser().getPassword())).thenReturn(true);
        when(userCredentialsGenerator.encodePassword("newSecurePassword")).thenReturn("newSecurePassword");


        service.changePassword(request);

        verify(traineeDAO).update(captor.capture());

        Trainee updated = captor.getValue();
        assertEquals("newSecurePassword", updated.getUser().getPassword());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO).update(any(Trainee.class));
    }

    @Test
    void deleteByUsername_ShouldCallDAODeleteByUsername() {
        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.deleteByUsername(USERNAME);

        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO).deleteByUsername(USERNAME);
    }

    @Test
    void deleteByUsername_ShouldThrowExceptionWhenTraineeNotFound() {
        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.deleteByUsername(USERNAME));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO, never()).deleteByUsername(USERNAME);
    }

    @Test
    void toggleTraineeActivation_ShouldSetActivationToTrue() {
        Trainee inactiveTrainee = buildInactiveTrainee();

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(inactiveTrainee));

        service.toggleTraineeActivation(USERNAME, true);

        verify(traineeDAO).update(captor.capture());

        Trainee captured = captor.getValue();
        assertTrue(captured.getUser().getIsActive());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO).update(any(Trainee.class));
    }

    @Test
    void toggleTraineeActivation_ShouldSetActivationToFalse() {
        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.toggleTraineeActivation(USERNAME, false);

        verify(traineeDAO).update(captor.capture());

        Trainee captured = captor.getValue();
        assertFalse(captured.getUser().getIsActive());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO).update(any(Trainee.class));
    }

    @Test
    void toggleTraineeActivation_ShouldThrowExceptionWhenTraineeNotFound() {
        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTraineeActivation(USERNAME, true));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(traineeDAO, never()).update(any());
    }

    @Test
    void updateTraineeTrainersList_ShouldUpdateSuccessfully() {
        TraineeTrainersUpdateRequestDto request = new TraineeTrainersUpdateRequestDto();
        request.setTrainerUsernames(List.of("trainer1", "trainer2"));

        Trainee trainee = buildTrainee();
        Trainer trainer1 = createTrainerWithUsername("trainer1");
        Trainer trainer2 = createTrainerWithUsername("trainer2");
        Trainee updatedTrainee = buildTrainee();
        TraineeTrainersUpdateResponseDto expected = buildTraineeTrainersUpdateResponse();

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(trainer1));
        when(trainerDAO.findByUsername("trainer2")).thenReturn(Optional.of(trainer2));
        when(traineeDAO.updateTraineeTrainersList(USERNAME, List.of("trainer1", "trainer2")))
                .thenReturn(updatedTrainee);
        when(traineeMapper.toTrainersUpdateResponse(updatedTrainee)).thenReturn(expected);

        TraineeTrainersUpdateResponseDto actual = service.updateTraineeTrainersList(request, USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getTrainers().getFirst().getUsername(), actual.getTrainers().getFirst().getUsername());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(trainerDAO).findByUsername("trainer1");
        verify(trainerDAO).findByUsername("trainer2");
        verify(traineeDAO).updateTraineeTrainersList(USERNAME, List.of("trainer1", "trainer2"));
        verify(traineeMapper).toTrainersUpdateResponse(updatedTrainee);
    }

    @Test
    void updateTraineeTrainersList_ShouldThrowExceptionWhenTraineeNotFound() {
        TraineeTrainersUpdateRequestDto request = new TraineeTrainersUpdateRequestDto();
        request.setTrainerUsernames(List.of("trainer1"));

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.updateTraineeTrainersList(request, USERNAME));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(trainerDAO, never()).findByUsername(any());
        verify(traineeDAO, never()).updateTraineeTrainersList(any(), any());
        verify(traineeMapper, never()).toTrainersUpdateResponse(any());
    }

    @Test
    void updateTraineeTrainersList_ShouldThrowExceptionWhenTrainerNotFound() {
        TraineeTrainersUpdateRequestDto request = new TraineeTrainersUpdateRequestDto();
        request.setTrainerUsernames(List.of("trainer1", "trainer2"));

        when(traineeDAO.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(createTrainerWithUsername("trainer1")));
        when(trainerDAO.findByUsername("trainer2")).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.updateTraineeTrainersList(request, USERNAME));

        assertEquals("Trainer not found with username: trainer2", exception.getMessage());
        verify(traineeDAO).findByUsername(USERNAME);
        verify(trainerDAO).findByUsername("trainer1");
        verify(trainerDAO).findByUsername("trainer2");
        verify(traineeDAO, never()).updateTraineeTrainersList(any(), any());
        verify(traineeMapper, never()).toTrainersUpdateResponse(any());
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(999L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(BIRTH_DATE)
                .address(ADDRESS)
                .build();
    }

    private Trainee buildInactiveTrainee() {
        User user = User.builder()
                .id(999L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(false)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(BIRTH_DATE)
                .address(ADDRESS)
                .build();
    }

    private Trainee buildUpdatedTrainee() {
        User user = buildTrainee().getUser().toBuilder()
                .firstName("Jane")
                .lastName("Smith")
                .isActive(false)
                .build();

        return buildTrainee().toBuilder()
                .user(user)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .address("456 Oak Ave")
                .build();
    }

    private TraineeCreateRequestDto buildTraineeCreateRequest() {
        TraineeCreateRequestDto request = new TraineeCreateRequestDto();
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);
        request.setDateOfBirth(BIRTH_DATE);
        request.setAddress(ADDRESS);

        return request;
    }

    private TraineeUpdateRequestDto buildTraineeUpdateRequest() {
        TraineeUpdateRequestDto request = new TraineeUpdateRequestDto();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(false);
        request.setDateOfBirth(LocalDate.of(1985, 5, 15));
        request.setAddress("456 Oak Ave");

        return request;
    }

    private TraineeGetResponseDto buildTraineeGetResponse() {
        TraineeGetResponseDto response = new TraineeGetResponseDto();
        response.setId(TRAINEE_ID);
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setUsername(USERNAME);
        response.setActive(true);
        response.setDateOfBirth(BIRTH_DATE);
        response.setAddress(ADDRESS);

        return response;
    }

    private TraineeUpdateResponseDto buildTraineeUpdateResponse() {
        TraineeUpdateResponseDto response = new TraineeUpdateResponseDto();
        response.setFirstName("Jane");
        response.setLastName("Smith");
        response.setActive(false);
        response.setDateOfBirth(LocalDate.of(1985, 5, 15));
        response.setAddress("456 Oak Ave");

        return response;
    }

    private TraineeTrainersUpdateResponseDto buildTraineeTrainersUpdateResponse() {
        TrainerModel trainerModel = TrainerModel.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();

        return TraineeTrainersUpdateResponseDto.builder()
                .trainers(List.of(trainerModel))
                .build();
    }

    private Trainee createTraineeWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .build();

        return Trainee.builder()
                .user(user)
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
}

package com.gym.crm.service.impl;

import com.gym.crm.actuator.prometheus.UserProfileMetrics;
import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
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
    private TraineeRepository traineeRepository;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private UserProfileMetrics userProfileMetrics;
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
        when(traineeRepository.findAll()).thenReturn(existingTrainees);
        when(userCredentialsGenerator.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames))
                .thenReturn(USERNAME);
        when(userCredentialsGenerator.generateRawPassword()).thenReturn(RAW_PASSWORD);
        when(userCredentialsGenerator.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        TraineeCreateResponseDto actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
        verify(traineeMapper).toEntity(createRequest);
        verify(traineeRepository).findAll();
        verify(userCredentialsGenerator).generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generateRawPassword();
        verify(userCredentialsGenerator).encodePassword(RAW_PASSWORD);
        verify(traineeRepository).save(any(Trainee.class));
        verify(traineeRepository).save(captor.capture());
        verify(userProfileMetrics).recordProfileCreation();

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

        when(traineeRepository.findById(TRAINEE_ID)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toResponse(trainee)).thenReturn(expected);

        Optional<TraineeGetResponseDto> actual = service.findById(TRAINEE_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getUsername(), actual.get().getUsername());
        verify(traineeRepository).findById(TRAINEE_ID);
        verify(traineeMapper).toResponse(trainee);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long traineeId = 999L;

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

        Optional<TraineeGetResponseDto> actual = service.findById(traineeId);

        assertFalse(actual.isPresent());
        verify(traineeRepository).findById(traineeId);
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void findByUsername_ShouldReturnTraineeWhenExists() {
        Trainee buildTrainee = buildTrainee();
        TraineeGetResponseDto expected = buildTraineeGetResponse();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(buildTrainee));
        when(traineeMapper.toResponse(buildTrainee)).thenReturn(expected);

        TraineeGetResponseDto actual = service.findByUsername(USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeMapper).toResponse(buildTrainee);
    }

    @Test
    void findByUsername_ShouldThrowExceptionWhenNotExists() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.findByUsername(USERNAME));

        assertEquals("Unable to find trainee with username: " + USERNAME, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTraineeSuccessfully() {
        TraineeUpdateRequestDto updateRequest = buildTraineeUpdateRequest();
        Trainee updatedTrainee = buildUpdatedTrainee();
        TraineeUpdateResponseDto expected = buildTraineeUpdateResponse();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toUpdateResponseDto(any(Trainee.class))).thenReturn(expected);

        TraineeUpdateResponseDto actual = service.update(updateRequest, USERNAME);

        assertNotNull(actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.isActive(), actual.isActive());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository).save(any(Trainee.class));
        verify(traineeMapper).toUpdateResponseDto(updatedTrainee);
        verify(traineeRepository).save(captor.capture());

        Trainee captured = captor.getValue();
        assertEquals("Jane", captured.getUser().getFirstName());
        assertEquals("Smith", captured.getUser().getLastName());
        assertFalse(captured.getUser().getIsActive());
        assertEquals(LocalDate.of(1985, 5, 15), captured.getDateOfBirth());
        assertEquals("456 Oak Ave", captured.getAddress());
        verify(userProfileMetrics).recordProfileUpdate();
    }

    @Test
    void update_ShouldThrowExceptionWhenTraineeNotFound() {
        TraineeUpdateRequestDto updateRequest = buildTraineeUpdateRequest();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.update(updateRequest, USERNAME));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository, never()).save(any());
        verify(traineeMapper, never()).toUpdateResponseDto(any());
        verify(userProfileMetrics, never()).recordProfileUpdate();
    }

    @Test
    void changePassword_ShouldUpdatePasswordWhenOldPasswordMatches() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword("newSecurePassword");

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
        when(userCredentialsGenerator.matches(PASSWORD, trainee.getUser().getPassword())).thenReturn(true);
        when(userCredentialsGenerator.encodePassword("newSecurePassword")).thenReturn("newSecurePassword");


        service.changePassword(request);

        verify(traineeRepository).save(captor.capture());

        Trainee updated = captor.getValue();
        assertEquals("newSecurePassword", updated.getUser().getPassword());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void deleteByUsername_ShouldCallDAODeleteByUsername() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));

        service.deleteByUsername(USERNAME);

        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository).deleteByUser_Username(USERNAME);
    }

    @Test
    void deleteByUsername_ShouldThrowExceptionWhenTraineeNotFound() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.deleteByUsername(USERNAME));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository, never()).deleteByUser_Username(USERNAME);
    }

    @Test
    void toggleTraineeActivation_ShouldSetActivationToTrue() {
        Trainee inactiveTrainee = buildInactiveTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(inactiveTrainee));

        service.toggleTraineeActivation(USERNAME, true);

        verify(traineeRepository).save(captor.capture());

        Trainee captured = captor.getValue();
        assertTrue(captured.getUser().getIsActive());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void toggleTraineeActivation_ShouldSetActivationToFalse() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));

        service.toggleTraineeActivation(USERNAME, false);

        verify(traineeRepository).save(captor.capture());

        Trainee captured = captor.getValue();
        assertFalse(captured.getUser().getIsActive());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void toggleTraineeActivation_ShouldThrowExceptionWhenTraineeNotFound() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTraineeActivation(USERNAME, true));

        assertEquals("Trainee not found with username: " + USERNAME, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void toggleTraineeActivation_ShouldThrowException_WhenTryingToActivateAlreadyActiveTrainee() {
        String expectedMessage = String.format("Trainee with username: %s is already active", USERNAME);
        Trainee activeTrainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(activeTrainee));

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTraineeActivation(USERNAME, true));

        assertEquals(expectedMessage, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void toggleTraineeActivation_ShouldThrowException_WhenTryingToDeactivateAlreadyInactiveTrainee() {
        String expectedMessage = String.format("Trainee with username: %s is already inactive", USERNAME);
        Trainee inactiveTrainee = buildInactiveTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(inactiveTrainee));

        CoreServiceException exception = assertThrows(CoreServiceException.class,
                () -> service.toggleTraineeActivation(USERNAME, false));

        assertEquals(expectedMessage, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(traineeRepository, never()).save(any(Trainee.class));
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

    private Trainee createTraineeWithUsername(String username) {
        User user = User.builder()
                .username(username)
                .build();

        return Trainee.builder()
                .user(user)
                .build();
    }
}

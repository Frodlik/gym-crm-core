package com.gym.crm.service.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.facade.GymTestObjects;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.util.UserCredentialsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private static final String TRAINER_FIRST_NAME = "Mike";
    private static final String TRAINER_LAST_NAME = "Johnson";
    private static final String TRAINER_USERNAME = "mike.johnson";
    private static final String PASSWORD = "password123";
    private static final String FITNESS_TYPE = "Fitness";
    private static final String YOGA_TYPE = "YOGA";
    private static final Long TRAINER_ID = 1L;
    private static final String GENERATED_PASSWORD = "generatedPassword";

    @Mock
    private TrainerDAO trainerDAO;
    @Mock
    private UserCredentialsGenerator userCredentialsGenerator;
    @Mock
    private TrainerMapper trainerMapper;
    @InjectMocks
    private TrainerServiceImpl service;

    private final Trainer trainer = buildTrainer();

    @Test
    void create_ShouldCreateTrainerSuccessfully() {
        TrainerCreateRequest createRequest = GymTestObjects.buildTrainerCreateRequest();
        List<Trainer> existingTrainers = List.of(
                createTrainerWithUsername("existing.trainer1"),
                createTrainerWithUsername("existing.trainer2")
        );
        List<String> existingUsernames = List.of("existing.trainer1", "existing.trainer2");
        TrainerResponse expected = GymTestObjects.buildTrainerResponse();

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(trainerDAO.create(trainer)).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        TrainerResponse actual = service.create(createRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getSpecialization(), actual.getSpecialization());

        verify(trainerMapper).toEntity(createRequest);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);
        verify(userCredentialsGenerator).generatePassword();
        verify(trainerDAO).create(trainer);
        verify(trainerMapper).toResponse(trainer);

        assertEquals(TRAINER_USERNAME, trainer.getUsername());
        assertEquals(GENERATED_PASSWORD, trainer.getPassword());
    }

    @Test
    void create_ShouldHandleEmptyExistingUsernames() {
        TrainerCreateRequest createRequest = GymTestObjects.buildTrainerCreateRequest();
        List<Trainer> existingTrainers = List.of();
        List<String> existingUsernames = List.of();
        TrainerResponse expectedResponse = GymTestObjects.buildTrainerResponse();

        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerDAO.findAll()).thenReturn(existingTrainers);
        when(userCredentialsGenerator.generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames))
                .thenReturn(TRAINER_USERNAME);
        when(userCredentialsGenerator.generatePassword()).thenReturn(GENERATED_PASSWORD);
        when(trainerDAO.create(trainer)).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(expectedResponse);

        TrainerResponse actual = service.create(createRequest);

        assertNotNull(actual);
        verify(trainerDAO).findAll();
        verify(userCredentialsGenerator).generateUsername(TRAINER_FIRST_NAME, TRAINER_LAST_NAME, existingUsernames);

    }

    @Test
    void findById_ShouldReturnTrainerWhenExists() {
        TrainerResponse expected = GymTestObjects.buildTrainerResponse();

        when(trainerDAO.findById(TRAINER_ID)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(expected);

        Optional<TrainerResponse> actual = service.findById(TRAINER_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
        assertEquals(expected.getUsername(), actual.get().getUsername());
        assertEquals(expected.getSpecialization(), actual.get().getSpecialization());

        verify(trainerDAO).findById(TRAINER_ID);
        verify(trainerMapper).toResponse(trainer);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Long trainerId = 999L;

        when(trainerDAO.findById(trainerId)).thenReturn(Optional.empty());

        Optional<TrainerResponse> result = service.findById(trainerId);

        assertFalse(result.isPresent());

        verify(trainerDAO).findById(trainerId);
        verify(trainerMapper, never()).toResponse(any());
    }

    @Test
    void update_ShouldUpdateTrainerSuccessfully() {
        TrainerUpdateRequest updateRequest = GymTestObjects.buildTrainerUpdateRequest();
        Trainer updatedTrainer = buildUpdatedTrainer();
        TrainerResponse expected = buildUpdatedResponse();

        when(trainerDAO.findById(updateRequest.getId())).thenReturn(Optional.of(trainer));
        when(trainerDAO.update(trainer)).thenReturn(updatedTrainer);
        when(trainerMapper.toResponse(updatedTrainer)).thenReturn(expected);

        TrainerResponse actual = service.update(updateRequest);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.isActive(), actual.isActive());
        assertEquals(expected.getSpecialization(), actual.getSpecialization());

        verify(trainerDAO).findById(updateRequest.getId());
        verify(trainerDAO).update(trainer);
        verify(trainerMapper).toResponse(updatedTrainer);

        assertEquals("Michael", trainer.getFirstName());
        assertEquals("Smith", trainer.getLastName());
        assertEquals(false, trainer.getIsActive());
        assertEquals(YOGA_TYPE, trainer.getSpecialization().getTrainingTypeName());
    }

    @Test
    void update_ShouldThrowExceptionWhenTrainerNotFound() {
        TrainerUpdateRequest updateRequest = GymTestObjects.buildTrainerUpdateRequest();

        when(trainerDAO.findById(updateRequest.getId())).thenReturn(Optional.empty());

        CoreServiceException exception = assertThrows(CoreServiceException.class, () -> service.update(updateRequest));

        assertEquals("Trainer not found with id: " + updateRequest.getId(), exception.getMessage());

        verify(trainerDAO).findById(updateRequest.getId());
        verify(trainerDAO, never()).update(any());
        verify(trainerMapper, never()).toResponse(any());
    }

    private Trainer buildTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUserId(TRAINER_ID);
        trainer.setFirstName(TRAINER_FIRST_NAME);
        trainer.setLastName(TRAINER_LAST_NAME);
        trainer.setUsername(TRAINER_USERNAME);
        trainer.setPassword(PASSWORD);
        trainer.setIsActive(true);
        trainer.setSpecialization(new TrainingType(FITNESS_TYPE));

        return trainer;
    }

    private Trainer buildUpdatedTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUserId(TRAINER_ID);
        trainer.setFirstName("Michael");
        trainer.setLastName("Smith");
        trainer.setIsActive(false);
        trainer.setSpecialization(new TrainingType(YOGA_TYPE));

        return trainer;
    }

    private TrainerResponse buildUpdatedResponse() {
        TrainerResponse response = new TrainerResponse();
        response.setId(TRAINER_ID);
        response.setFirstName("Michael");
        response.setLastName("Smith");
        response.setActive(false);
        response.setSpecialization(new TrainingType(YOGA_TYPE));

        return response;
    }

    private Trainer createTrainerWithUsername(String username) {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);

        return trainer;
    }
}

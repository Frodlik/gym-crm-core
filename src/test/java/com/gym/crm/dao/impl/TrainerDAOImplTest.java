package com.gym.crm.dao.impl;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TrainerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerDAOImplTest {
    private static final Long TRAINER_ID = 1L;
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String USERNAME = "jane.smith";
    private static final String PASSWORD = "password456";
    private static final TrainingType DEFAULT_SPECIALIZATION = new TrainingType("Yoga");

    @Mock
    private InMemoryStorage inMemoryStorage;
    @Mock
    private TrainerStorage trainerStorage;
    @InjectMocks
    private TrainerDAOImpl dao;

    @BeforeEach
    void setUp() {
        when(inMemoryStorage.getTrainerStorage()).thenReturn(trainerStorage);
        dao.setStorage(inMemoryStorage);
    }

    @Test
    void testCreate_ShouldCreateTrainerWithGeneratedId() {
        Trainer trainer = createTrainer();

        when(trainerStorage.getNextId()).thenReturn(TRAINER_ID);
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Trainer result = dao.create(trainer);

        assertNotNull(result);
        assertEquals(TRAINER_ID, result.getUserId());
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertTrue(result.getIsActive());
        assertEquals(DEFAULT_SPECIALIZATION, result.getSpecialization());
    }

    @Test
    void testCreate_ShouldCreateTrainerWithNullSpecialization() {
        Trainer trainer = createTrainer(null, false);

        when(trainerStorage.getNextId()).thenReturn(2L);
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Trainer result = dao.create(trainer);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertFalse(result.getIsActive());
        assertNull(result.getSpecialization());
    }

    @Test
    void testFindById_ShouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainerWithId(TRAINER_ID);
        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(TRAINER_ID, trainer);

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        Optional<Trainer> result = dao.findById(TRAINER_ID);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        assertEquals(TRAINER_ID, result.get().getUserId());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;

        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Optional<Trainer> result = dao.findById(id);

        assertFalse(result.isPresent());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        Trainer trainer1 = createTrainerWithId(1L);
        Trainer trainer2 = createTrainerWithId(2L);
        trainer2.setFirstName("Bob");
        trainer2.setUsername("bob.smith");
        trainer2.setSpecialization(new TrainingType("Pilates"));

        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(1L, trainer1);
        trainersMap.put(2L, trainer2);

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        List<Trainer> result = dao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(trainer1));
        assertTrue(result.contains(trainer2));
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainers() {
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        List<Trainer> result = dao.findAll();

        assertTrue(result.isEmpty());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainer() {
        Trainer existingTrainer = createTrainerWithId(TRAINER_ID);
        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(TRAINER_ID, existingTrainer);

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        Trainer updatedTrainer = createTrainerWithId(TRAINER_ID);
        updatedTrainer.setFirstName("Jane Updated");
        updatedTrainer.setIsActive(false);
        updatedTrainer.setSpecialization(new TrainingType("Pilates"));

        Trainer result = dao.update(updatedTrainer);

        assertEquals(updatedTrainer, result);
        assertEquals("Jane Updated", result.getFirstName());
        assertFalse(result.getIsActive());
        assertEquals("Pilates", result.getSpecialization().getTrainingTypeName());
        verify(trainerStorage, times(1)).getTrainers();
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTrainerNotExists() {
        Trainer trainer = createTrainerWithId(999L);

        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dao.update(trainer)
        );

        assertEquals("Trainer not found with ID: 999", exception.getMessage());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testSetStorage_ShouldInitializeTrainerStorage() {
        InMemoryStorage newStorage = mock(InMemoryStorage.class);
        TrainerStorage newTrainerStorage = mock(TrainerStorage.class);

        when(newStorage.getTrainerStorage()).thenReturn(newTrainerStorage);

        dao.setStorage(newStorage);

        verify(newStorage).getTrainerStorage();
    }

    private Trainer createTrainer() {
        return createTrainer(DEFAULT_SPECIALIZATION, true);
    }

    private Trainer createTrainer(TrainingType specialization, boolean isActive) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(FIRST_NAME);
        trainer.setLastName(LAST_NAME);
        trainer.setUsername(USERNAME);
        trainer.setPassword(PASSWORD);
        trainer.setIsActive(isActive);
        trainer.setSpecialization(specialization);
        return trainer;
    }

    private Trainer createTrainerWithId(Long id) {
        Trainer trainer = createTrainer();
        trainer.setUserId(id);

        return trainer;
    }
}

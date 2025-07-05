package com.gym.crm.dao.impl;

import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
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
    private static final TrainingType DEFAULT_SPECIALIZATION = TrainingType.builder().trainingTypeName("Yoga").build();

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

        Trainer actual = dao.create(trainer);

        assertNotNull(actual);
        assertEquals(TRAINER_ID, actual.getId());
        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertTrue(actual.getUser().getIsActive());
        assertEquals(DEFAULT_SPECIALIZATION, actual.getSpecialization());
    }

    @Test
    void testCreate_ShouldCreateTrainerWithNullSpecialization() {
        Trainer trainer = createTrainer(null, false);

        when(trainerStorage.getNextId()).thenReturn(2L);
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Trainer actual = dao.create(trainer);

        assertNotNull(actual);
        assertEquals(2L, actual.getId());
        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertFalse(actual.getUser().getIsActive());
        assertNull(actual.getSpecialization());
    }

    @Test
    void testFindById_ShouldReturnTrainerWhenExists() {
        Trainer expected = createTrainerWithId(TRAINER_ID);
        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(TRAINER_ID, expected);

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        Optional<Trainer> actual = dao.findById(TRAINER_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        assertEquals(TRAINER_ID, actual.get().getId());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;

        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        Optional<Trainer> actual = dao.findById(id);

        assertFalse(actual.isPresent());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainers() {
        Trainer trainer1 = createTrainerWithId(1L);
        Trainer trainer2 = createTrainerWithId(2L);
        User user = trainer2.getUser().toBuilder()
                .firstName("Bob")
                .username("bob.smith")
                .build();

        trainer2.toBuilder()
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName("Pilates").build())
                .build();

        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(1L, trainer1);
        trainersMap.put(2L, trainer2);

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);

        List<Trainer> actual = dao.findAll();

        assertEquals(2, actual.size());
        assertTrue(actual.contains(trainer1));
        assertTrue(actual.contains(trainer2));
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainers() {
        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        List<Trainer> actual = dao.findAll();

        assertTrue(actual.isEmpty());
        verify(trainerStorage).getTrainers();
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainer() {
        Trainer existingTrainer = createTrainerWithId(TRAINER_ID);
        Map<Long, Trainer> trainersMap = new ConcurrentHashMap<>();
        trainersMap.put(TRAINER_ID, existingTrainer);

        when(trainerStorage.getTrainers()).thenReturn(trainersMap);
        User updatedUser = existingTrainer.getUser().toBuilder()
                .firstName("John Updated")
                .isActive(false)
                .build();

        Trainer expected = createTrainerWithId(TRAINER_ID).toBuilder()
                .user(updatedUser)
                .specialization(TrainingType.builder().trainingTypeName("Pilates").build())
                .build();

        Trainer actual = dao.update(expected);

        assertEquals(expected, actual);
        assertEquals(expected.getUser().getFirstName(), actual.getUser().getFirstName());
        assertFalse(actual.getUser().getIsActive());
        assertEquals(expected.getSpecialization().getTrainingTypeName(), actual.getSpecialization().getTrainingTypeName());
        verify(trainerStorage, times(1)).getTrainers();
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTrainerNotExists() {
        Trainer trainer = createTrainerWithId(999L);

        when(trainerStorage.getTrainers()).thenReturn(new ConcurrentHashMap<>());

        DaoException exception = assertThrows(DaoException.class, () -> dao.update(trainer));

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
        User user = User.builder()
                .id(1000L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(isActive)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();
    }

    private Trainer createTrainerWithId(Long id) {
        Trainer trainer = createTrainer();

        return trainer.toBuilder()
                .id(id)
                .build();
    }
}

package com.gym.crm.dao.impl;

import com.gym.crm.model.Trainee;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TraineeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeDAOImplTest {
    private static final Long TRAINEE_ID = 1L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";

    @Mock
    private InMemoryStorage inMemoryStorage;
    @Mock
    private TraineeStorage traineeStorage;
    @InjectMocks
    private TraineeDAOImpl dao;

    @BeforeEach
    void setUp() {
        when(inMemoryStorage.getTraineeStorage()).thenReturn(traineeStorage);
        dao.setStorage(inMemoryStorage);
    }

    @Test
    void testCreate_ShouldCreateTraineeWithGeneratedId() {
        Trainee trainee = createTraineeWithoutId(FIRST_NAME, LAST_NAME, USERNAME,
                DATE_OF_BIRTH, ADDRESS, true);

        when(traineeStorage.getNextId()).thenReturn(TRAINEE_ID);
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        Trainee actual = dao.create(trainee);

        assertNotNull(actual);
        assertEquals(TRAINEE_ID, actual.getUserId());
        assertEquals(FIRST_NAME, actual.getFirstName());
        assertEquals(LAST_NAME, actual.getLastName());
        assertEquals(USERNAME, actual.getUsername());
        assertEquals(PASSWORD, actual.getPassword());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());
        assertTrue(actual.getIsActive());

        verify(traineeStorage).getNextId();
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testCreate_ShouldCreateTraineeWithNullAddress() {
        Trainee trainee = createTraineeWithoutId(FIRST_NAME, LAST_NAME, USERNAME,
                DATE_OF_BIRTH, null, false);

        when(traineeStorage.getNextId()).thenReturn(TRAINEE_ID);
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        Trainee actual = dao.create(trainee);

        assertNotNull(actual);
        assertEquals(TRAINEE_ID, actual.getUserId());
        assertEquals(FIRST_NAME, actual.getFirstName());
        assertEquals(LAST_NAME, actual.getLastName());
        assertEquals(USERNAME, actual.getUsername());
        assertEquals(PASSWORD, actual.getPassword());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertNull(actual.getAddress());
        assertFalse(actual.getIsActive());
    }

    @Test
    void testFindById_ShouldReturnTraineeWhenExists() {
        Trainee expected = createSampleTrainee(TRAINEE_ID);
        Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(TRAINEE_ID, expected);

        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        Optional<Trainee> actual = dao.findById(TRAINEE_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        assertEquals(TRAINEE_ID, actual.get().getUserId());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;

        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        Optional<Trainee> actual = dao.findById(id);

        assertFalse(actual.isPresent());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainees() {
        Trainee trainee1 = createSampleTrainee(1L);
        Trainee trainee2 = createSampleTrainee(2L);
        trainee2.setFirstName("Jane");
        trainee2.setUsername("jane.doe");

        Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(1L, trainee1);
        traineesMap.put(2L, trainee2);

        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        List<Trainee> actual = dao.findAll();

        assertEquals(2, actual.size());
        assertTrue(actual.contains(trainee1));
        assertTrue(actual.contains(trainee2));
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainees() {
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        List<Trainee> actual = dao.findAll();

        assertTrue(actual.isEmpty());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainee() {
        Trainee existingTrainee = createSampleTrainee(TRAINEE_ID);
        Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(TRAINEE_ID, existingTrainee);

        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        Trainee expected = createSampleTrainee(TRAINEE_ID);
        expected.setFirstName("John Updated");
        expected.setAddress("456 Oak Ave");
        expected.setIsActive(false);

        Trainee actual = dao.update(expected);

        assertEquals(expected, actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertFalse(actual.getIsActive());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTraineeNotExists() {
        Trainee trainee = createSampleTrainee(999L);

        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> dao.update(trainee));

        assertEquals("Trainee not found with ID: 999", exception.getMessage());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testDelete_ShouldReturnTrueWhenTraineeExists() {
        Trainee trainee = createSampleTrainee(TRAINEE_ID);
        Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(TRAINEE_ID, trainee);

        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        boolean result = dao.delete(TRAINEE_ID);

        assertTrue(result);
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testDelete_ShouldReturnFalseWhenTraineeNotExists() {
        Long id = 999L;

        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        boolean result = dao.delete(id);

        assertFalse(result);
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testSetStorage_ShouldInitializeTraineeStorage() {
        InMemoryStorage newStorage = mock(InMemoryStorage.class);
        TraineeStorage newTraineeStorage = mock(TraineeStorage.class);

        when(newStorage.getTraineeStorage()).thenReturn(newTraineeStorage);

        dao.setStorage(newStorage);

        verify(newStorage).getTraineeStorage();
    }

    private Trainee createSampleTrainee(Long id) {
        Trainee trainee = new Trainee();
        trainee.setUserId(id);
        trainee.setFirstName(FIRST_NAME);
        trainee.setLastName(LAST_NAME);
        trainee.setUsername(USERNAME);
        trainee.setPassword(PASSWORD);
        trainee.setDateOfBirth(DATE_OF_BIRTH);
        trainee.setAddress(ADDRESS);
        trainee.setIsActive(true);

        return trainee;
    }

    private Trainee createTraineeWithoutId(String firstName, String lastName, String username,
                                           LocalDate dateOfBirth, String address, Boolean isActive) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setUsername(username);
        trainee.setPassword(PASSWORD);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setIsActive(isActive);

        return trainee;
    }
}

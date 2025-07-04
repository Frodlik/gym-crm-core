package com.gym.crm.dao.impl;

import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
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
        assertEquals(TRAINEE_ID, actual.getId());
        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());
        assertTrue(actual.getUser().getIsActive());

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
        assertEquals(TRAINEE_ID, actual.getId());
        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertNull(actual.getAddress());
        assertFalse(actual.getUser().getIsActive());
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
        assertEquals(TRAINEE_ID, actual.get().getId());
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
        User saved = trainee2.getUser().toBuilder()
                .firstName("Jane")
                .username("jane.doe")
                .build();
        trainee2.toBuilder()
                .user(saved)
                .build();

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

        User updatedUser = existingTrainee.getUser().toBuilder()
                .firstName("John Updated")
                .isActive(false)
                .build();

        Trainee updatedTrainee = existingTrainee.toBuilder()
                .user(updatedUser)
                .address("456 Oak Ave")
                .build();

        Trainee actual = dao.update(updatedTrainee);

        assertEquals("John Updated", actual.getUser().getFirstName());
        assertEquals("456 Oak Ave", actual.getAddress());
        assertFalse(actual.getUser().getIsActive());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTraineeNotExists() {
        Trainee trainee = createSampleTrainee(999L);

        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        DaoException exception = assertThrows(DaoException.class, () -> dao.update(trainee));

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
        User user = User.builder()
                .id(1000L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(id)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();
    }

    private Trainee createTraineeWithoutId(String firstName, String lastName, String username,
                                           LocalDate dateOfBirth, String address, Boolean isActive) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(PASSWORD)
                .isActive(isActive)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();
    }
}

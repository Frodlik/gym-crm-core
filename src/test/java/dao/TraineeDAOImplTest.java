package dao;

import com.gym.crm.dao.impl.TraineeDAOImpl;
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
class TraineeDAOImplTest {
    @Mock
    private InMemoryStorage inMemoryStorage;
    @Mock
    private TraineeStorage traineeStorage;
    @InjectMocks
    private TraineeDAOImpl traineeDAO;

    @BeforeEach
    void setUp() {
        when(inMemoryStorage.getTraineeStorage()).thenReturn(traineeStorage);
        traineeDAO.setStorage(inMemoryStorage);
    }

    @Test
    void testCreate_ShouldCreateTraineeWithGeneratedId() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("john.doe");
        trainee.setPassword("password123");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");
        trainee.setIsActive(true);

        when(traineeStorage.getNextId()).thenReturn(1L);
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        Trainee result = traineeDAO.create(trainee);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals("123 Main St", result.getAddress());
        assertTrue(result.getIsActive());

        verify(traineeStorage).getNextId();
        verify(traineeStorage, times(1)).getTrainees();
    }

    @Test
    void testCreate_ShouldCreateTraineeWithNullAddress() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Jane");
        trainee.setLastName("Smith");
        trainee.setUsername("jane.smith");
        trainee.setPassword("password456");
        trainee.setDateOfBirth(LocalDate.of(1985, 5, 15));
        trainee.setIsActive(false);

        when(traineeStorage.getNextId()).thenReturn(2L);
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        Trainee result = traineeDAO.create(trainee);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane.smith", result.getUsername());
        assertEquals("password456", result.getPassword());
        assertEquals(LocalDate.of(1985, 5, 15), result.getDateOfBirth());
        assertNull(result.getAddress());
        assertFalse(result.getIsActive());
    }

    @Test
    void testFindById_ShouldReturnTraineeWhenExists() {
        Long id = 1L;
        Trainee trainee = createSampleTrainee(id);

        java.util.Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(id, trainee);
        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        Optional<Trainee> result = traineeDAO.findById(id);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        assertEquals(id, result.get().getUserId());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        Optional<Trainee> result = traineeDAO.findById(id);

        assertFalse(result.isPresent());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainees() {
        Trainee trainee1 = createSampleTrainee(1L);
        Trainee trainee2 = createSampleTrainee(2L);
        trainee2.setFirstName("Jane");
        trainee2.setUsername("jane.doe");

        java.util.Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(1L, trainee1);
        traineesMap.put(2L, trainee2);
        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        List<Trainee> result = traineeDAO.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(trainee1));
        assertTrue(result.contains(trainee2));
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainees() {
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        List<Trainee> result = traineeDAO.findAll();

        assertTrue(result.isEmpty());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testUpdate_ShouldUpdateExistingTrainee() {
        Long id = 1L;
        Trainee existingTrainee = createSampleTrainee(id);

        java.util.Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(id, existingTrainee);
        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        Trainee updatedTrainee = createSampleTrainee(id);
        updatedTrainee.setFirstName("John Updated");
        updatedTrainee.setAddress("456 Oak Ave");
        updatedTrainee.setIsActive(false);

        Trainee result = traineeDAO.update(updatedTrainee);

        assertEquals(updatedTrainee, result);
        assertEquals("John Updated", result.getFirstName());
        assertEquals("456 Oak Ave", result.getAddress());
        assertFalse(result.getIsActive());
        verify(traineeStorage, times(1)).getTrainees();
    }

    @Test
    void testUpdate_ShouldThrowExceptionWhenTraineeNotExists() {
        Trainee trainee = createSampleTrainee(999L);
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> traineeDAO.update(trainee)
        );

        assertEquals("Trainee not found with ID: 999", exception.getMessage());
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testDelete_ShouldReturnTrueWhenTraineeExists() {
        Long id = 1L;
        Trainee trainee = createSampleTrainee(id);

        java.util.Map<Long, Trainee> traineesMap = new ConcurrentHashMap<>();
        traineesMap.put(id, trainee);
        when(traineeStorage.getTrainees()).thenReturn(traineesMap);

        boolean result = traineeDAO.delete(id);

        assertTrue(result);
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testDelete_ShouldReturnFalseWhenTraineeNotExists() {
        Long id = 999L;
        when(traineeStorage.getTrainees()).thenReturn(new ConcurrentHashMap<>());

        boolean result = traineeDAO.delete(id);

        assertFalse(result);
        verify(traineeStorage).getTrainees();
    }

    @Test
    void testSetStorage_ShouldInitializeTraineeStorage() {
        InMemoryStorage newStorage = mock(InMemoryStorage.class);
        TraineeStorage newTraineeStorage = mock(TraineeStorage.class);
        when(newStorage.getTraineeStorage()).thenReturn(newTraineeStorage);

        traineeDAO.setStorage(newStorage);

        verify(newStorage).getTraineeStorage();
    }

    private Trainee createSampleTrainee(Long id) {
        Trainee trainee = new Trainee();
        trainee.setUserId(id);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("john.doe");
        trainee.setPassword("password123");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        trainee.setAddress("123 Main St");
        trainee.setIsActive(true);
        return trainee;
    }
}

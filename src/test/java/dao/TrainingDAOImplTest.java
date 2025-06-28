package dao;

import com.gym.crm.dao.impl.TrainingDAOImpl;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TrainingStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDAOImplTest {
    @Mock
    private InMemoryStorage inMemoryStorage;
    @Mock
    private TrainingStorage trainingStorage;
    @InjectMocks
    private TrainingDAOImpl trainingDAO;

    @BeforeEach
    void setUp() {
        when(inMemoryStorage.getTrainingStorage()).thenReturn(trainingStorage);
        trainingDAO.setStorage(inMemoryStorage);
    }

    @Test
    void testCreate_ShouldCreateTrainingWithGeneratedId() {
        TrainingType trainingType = new TrainingType("Yoga");
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setTrainingName("Morning Yoga Session");
        training.setTrainingType(trainingType);
        training.setTrainingDate(LocalDate.of(2024, 1, 15));
        training.setDuration(60);

        when(trainingStorage.getNextId()).thenReturn(1L);
        when(trainingStorage.getTrainings()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertEquals(1L, result.getTraineeId());
        assertEquals(2L, result.getTrainerId());
        assertEquals("Morning Yoga Session", result.getTrainingName());
        assertEquals(trainingType, result.getTrainingType());
        assertEquals(LocalDate.of(2024, 1, 15), result.getTrainingDate());
        assertEquals(60, result.getDuration());

        verify(trainingStorage).getNextId();
        verify(trainingStorage, times(1)).getTrainings();
    }

    @Test
    void testCreate_ShouldCreateTrainingWithNullTrainingType() {
        Training training = new Training();
        training.setTraineeId(3L);
        training.setTrainerId(4L);
        training.setTrainingName("General Training");
        training.setTrainingDate(LocalDate.of(2024, 2, 20));
        training.setDuration(90);

        when(trainingStorage.getNextId()).thenReturn(2L);
        when(trainingStorage.getTrainings()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertEquals(3L, result.getTraineeId());
        assertEquals(4L, result.getTrainerId());
        assertEquals("General Training", result.getTrainingName());
        assertNull(result.getTrainingType());
        assertEquals(LocalDate.of(2024, 2, 20), result.getTrainingDate());
        assertEquals(90, result.getDuration());
    }

    @Test
    void testFindById_ShouldReturnTrainingWhenExists() {
        Long id = 1L;
        Training training = createSampleTraining();

        java.util.Map<Long, Training> trainingsMap = new java.util.concurrent.ConcurrentHashMap<>();
        trainingsMap.put(id, training);
        when(trainingStorage.getTrainings()).thenReturn(trainingsMap);

        Optional<Training> result = trainingDAO.findById(id);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        Long id = 999L;
        when(trainingStorage.getTrainings()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        Optional<Training> result = trainingDAO.findById(id);

        assertFalse(result.isPresent());
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainings() {
        Training training1 = createSampleTraining();
        Training training2 = createSampleTraining();
        training2.setTrainingName("Evening Pilates");
        training2.setTrainingType(new TrainingType("Pilates"));
        training2.setDuration(75);

        java.util.Map<Long, Training> trainingsMap = new java.util.concurrent.ConcurrentHashMap<>();
        trainingsMap.put(1L, training1);
        trainingsMap.put(2L, training2);
        when(trainingStorage.getTrainings()).thenReturn(trainingsMap);

        List<Training> result = trainingDAO.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(training1));
        assertTrue(result.contains(training2));
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testFindAll_ShouldReturnEmptyListWhenNoTrainings() {
        when(trainingStorage.getTrainings()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        List<Training> result = trainingDAO.findAll();

        assertTrue(result.isEmpty());
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testCreate_ShouldHandleTrainingWithMinimalData() {
        Training training = new Training();
        training.setTraineeId(5L);
        training.setTrainerId(6L);
        training.setTrainingName("Quick Session");
        training.setTrainingDate(LocalDate.now());
        training.setDuration(30);

        when(trainingStorage.getNextId()).thenReturn(3L);
        when(trainingStorage.getTrainings()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertEquals(5L, result.getTraineeId());
        assertEquals(6L, result.getTrainerId());
        assertEquals("Quick Session", result.getTrainingName());
        assertEquals(LocalDate.now(), result.getTrainingDate());
        assertEquals(30, result.getDuration());
        assertNull(result.getTrainingType());
    }

    @Test
    void testSetStorage_ShouldInitializeTrainingStorage() {
        InMemoryStorage newStorage = mock(InMemoryStorage.class);
        TrainingStorage newTrainingStorage = mock(TrainingStorage.class);
        when(newStorage.getTrainingStorage()).thenReturn(newTrainingStorage);

        trainingDAO.setStorage(newStorage);

        verify(newStorage).getTrainingStorage();
    }

    private Training createSampleTraining() {
        Training training = new Training();
        training.setTraineeId(1L);
        training.setTrainerId(2L);
        training.setTrainingName("Morning Yoga Session");
        training.setTrainingType(new TrainingType("Yoga"));
        training.setTrainingDate(LocalDate.of(2024, 1, 15));
        training.setDuration(60);
        return training;
    }
}

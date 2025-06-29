package com.gym.crm.dao.impl;

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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDAOImplTest {
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final String TRAINING_NAME = "Morning Yoga Session";
    private static final TrainingType TRAINING_TYPE = new TrainingType("Yoga");
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    private static final int DURATION = 60;

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
        Training training = createTraining(TRAINEE_ID, TRAINER_ID, TRAINING_NAME, TRAINING_TYPE, TRAINING_DATE, DURATION);

        when(trainingStorage.getNextId()).thenReturn(1L);
        when(trainingStorage.getTrainings()).thenReturn(new ConcurrentHashMap<>());

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertEquals(TRAINEE_ID, result.getTraineeId());
        assertEquals(TRAINER_ID, result.getTrainerId());
        assertEquals(TRAINING_NAME, result.getTrainingName());
        assertEquals(TRAINING_TYPE, result.getTrainingType());
        assertEquals(TRAINING_DATE, result.getTrainingDate());
        assertEquals(DURATION, result.getDuration());

        verify(trainingStorage).getNextId();
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testCreate_ShouldCreateTrainingWithNullTrainingType() {
        Training training = createTraining(3L, 4L, "General Training", null,
                LocalDate.of(2024, 2, 20), 90);

        when(trainingStorage.getNextId()).thenReturn(2L);
        when(trainingStorage.getTrainings()).thenReturn(new ConcurrentHashMap<>());

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
        Map<Long, Training> trainingsMap = new ConcurrentHashMap<>();
        trainingsMap.put(id, training);

        when(trainingStorage.getTrainings()).thenReturn(trainingsMap);

        Optional<Training> result = trainingDAO.findById(id);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testFindById_ShouldReturnEmptyWhenNotExists() {
        when(trainingStorage.getTrainings()).thenReturn(new ConcurrentHashMap<>());

        Optional<Training> result = trainingDAO.findById(999L);

        assertFalse(result.isPresent());
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testFindAll_ShouldReturnAllTrainings() {
        Training training1 = createSampleTraining();
        Training training2 = createTraining(3L, 4L, "Evening Pilates", new TrainingType("Pilates"),
                TRAINING_DATE, 75);


        Map<Long, Training> trainingsMap = new ConcurrentHashMap<>();
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
        when(trainingStorage.getTrainings()).thenReturn(new ConcurrentHashMap<>());

        List<Training> result = trainingDAO.findAll();

        assertTrue(result.isEmpty());
        verify(trainingStorage).getTrainings();
    }

    @Test
    void testCreate_ShouldHandleTrainingWithMinimalData() {
        LocalDate today = LocalDate.now();
        Training training = createTraining(5L, 6L, "Quick Session", null, today, 30);

        when(trainingStorage.getNextId()).thenReturn(3L);
        when(trainingStorage.getTrainings()).thenReturn(new ConcurrentHashMap<>());

        Training result = trainingDAO.create(training);

        assertNotNull(result);
        assertEquals(5L, result.getTraineeId());
        assertEquals(6L, result.getTrainerId());
        assertEquals("Quick Session", result.getTrainingName());
        assertEquals(today, result.getTrainingDate());
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
        return createTraining(TRAINEE_ID, TRAINER_ID, TRAINING_NAME, TRAINING_TYPE, TRAINING_DATE, DURATION);
    }

    private Training createTraining(Long traineeId, Long trainerId, String name, TrainingType type, LocalDate date, int duration) {
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(name);
        training.setTrainingType(type);
        training.setTrainingDate(date);
        training.setDuration(duration);
        return training;
    }
}

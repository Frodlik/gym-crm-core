package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataFileLoaderTest {
    private static final String TEST_DATA_FILE = "test-data.txt";

    private static final String CARDIO_TYPE = "CARDIO";
    private static final String POWERLIFTING_TYPE = "POWERLIFTING";
    private static final String DANCE_TYPE = "DANCE";
    private static final String MOBILITY_TYPE = "MOBILITY";
    private static final String CROSSFIT_TYPE = "CROSSFIT";

    private DataFileLoader dataFileLoader;

    @BeforeEach
    void setUp() {
        dataFileLoader = new DataFileLoader();
        ReflectionTestUtils.setField(dataFileLoader, "initDataFilePath", TEST_DATA_FILE);
    }

    @Test
    void shouldLoadTrainingTypesFromFile() {
        TrainingTypeStorage storage = new TrainingTypeStorage();
        dataFileLoader.loadTrainingTypesFromFile(storage);

        Map<String, TrainingType> actual = storage.getTrainingTypes();
        assertEquals(5, actual.size());
        assertTrue(actual.containsKey(CARDIO_TYPE));
        assertTrue(actual.containsKey(POWERLIFTING_TYPE));
        assertTrue(actual.containsKey(DANCE_TYPE));
        assertTrue(actual.containsKey(MOBILITY_TYPE));
        assertTrue(actual.containsKey(CROSSFIT_TYPE));
    }

    @Test
    void shouldLoadTraineesFromFile() {
        TraineeStorage storage = new TraineeStorage();
        dataFileLoader.loadTraineesFromFile(storage);

        Map<Long, Trainee> trainees = storage.getTrainees();
        assertEquals(5, trainees.size());

        Trainee actual = trainees.get(1L);
        assertNotNull(actual);
        assertEquals("Alice", actual.getFirstName());
        assertEquals("Walker", actual.getLastName());
        assertEquals("Alice.Walker", actual.getUsername());
        assertTrue(actual.getIsActive());
    }

    @Test
    void shouldLoadTrainersFromFile() {
        TrainingTypeStorage typeStorage = new TrainingTypeStorage();
        dataFileLoader.loadTrainingTypesFromFile(typeStorage);

        TrainerStorage storage = new TrainerStorage();
        dataFileLoader.loadTrainersFromFile(storage, typeStorage);

        Map<Long, Trainer> trainers = storage.getTrainers();
        assertEquals(5, trainers.size());

        Trainer actual = trainers.get(1L);
        assertNotNull(actual);
        assertEquals("Tom", actual.getFirstName());
        assertEquals(CARDIO_TYPE, actual.getSpecialization().getTrainingTypeName());
    }

    @Test
    void shouldLoadTrainingsFromFile() {
        TrainingTypeStorage typeStorage = new TrainingTypeStorage();
        dataFileLoader.loadTrainingTypesFromFile(typeStorage);

        TrainingStorage storage = new TrainingStorage();
        dataFileLoader.loadTrainingsFromFile(storage, typeStorage);

        Map<Long, Training> trainings = storage.getTrainings();
        assertEquals(8, trainings.size());

        Training actual = trainings.get(1L);
        assertNotNull(actual);
        assertEquals("Cardio Blast", actual.getTrainingName());
        assertEquals(CARDIO_TYPE, actual.getTrainingType().getTrainingTypeName());
    }
}


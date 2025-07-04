package com.gym.crm.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class InMemoryStorageTest {
    @Mock
    private TraineeStorage traineeStorage;
    @Mock
    private TrainerStorage trainerStorage;
    @Mock
    private TrainingStorage trainingStorage;
    @Mock
    private TrainingTypeStorage trainingTypeStorage;
    @InjectMocks
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage.setTraineeStorage(traineeStorage);
        storage.setTrainerStorage(trainerStorage);
        storage.setTrainingStorage(trainingStorage);
        storage.setTrainingTypeStorage(trainingTypeStorage);
    }

    @Test
    void setTraineeStorage_ShouldStoreTraineeStorage() {
        TraineeStorage newTraineeStorage = new TraineeStorage();

        storage.setTraineeStorage(newTraineeStorage);

        assertSame(newTraineeStorage, storage.getTraineeStorage());
    }

    @Test
    void setTrainerStorage_ShouldStoreTrainerStorage() {
        TrainerStorage newTrainerStorage = new TrainerStorage();

        storage.setTrainerStorage(newTrainerStorage);

        assertSame(newTrainerStorage, storage.getTrainerStorage());
    }

    @Test
    void setTrainingStorage_ShouldStoreTrainingStorage() {
        TrainingStorage newTrainingStorage = new TrainingStorage();

        storage.setTrainingStorage(newTrainingStorage);

        assertSame(newTrainingStorage, storage.getTrainingStorage());
    }

    @Test
    void setTrainingTypeStorage_ShouldStoreTrainingTypeStorage() {
        TrainingTypeStorage newTrainingTypeStorage = new TrainingTypeStorage();

        storage.setTrainingTypeStorage(newTrainingTypeStorage);

        assertSame(newTrainingTypeStorage, storage.getTrainingTypeStorage());
    }

    @Test
    void getTraineeStorage_ShouldReturnTraineeStorage() {
        TraineeStorage result = storage.getTraineeStorage();

        assertSame(traineeStorage, result);
    }

    @Test
    void getTrainerStorage_ShouldReturnTrainerStorage() {
        TrainerStorage result = storage.getTrainerStorage();

        assertSame(trainerStorage, result);
    }

    @Test
    void getTrainingStorage_ShouldReturnTrainingStorage() {
        TrainingStorage result = storage.getTrainingStorage();

        assertSame(trainingStorage, result);
    }

    @Test
    void getTrainingTypeStorage_ShouldReturnTrainingTypeStorage() {
        TrainingTypeStorage result = storage.getTrainingTypeStorage();

        assertSame(trainingTypeStorage, result);
    }

    @Test
    void getStorage_ShouldThrowExceptionWhenStorageTypeMismatch() {
        storage.setTraineeStorage(traineeStorage);

        assertNotNull(storage.getTraineeStorage());
        assertNotNull(storage.getTrainerStorage());
        assertNotNull(storage.getTrainingStorage());
        assertNotNull(storage.getTrainingTypeStorage());
    }

    @Test
    void getStorages_ShouldReturnStoragesMap() {
        assertNotNull(storage.getStorages());
        assertEquals(4, storage.getStorages().size());
    }

    @Test
    void getStorage_ShouldHandleWrongStorageType() {
        TraineeStorage traineeResult = storage.getTraineeStorage();
        TrainerStorage trainerResult = storage.getTrainerStorage();
        TrainingStorage trainingResult = storage.getTrainingStorage();
        TrainingTypeStorage trainingTypeResult = storage.getTrainingTypeStorage();

        assertSame(traineeStorage, traineeResult);
        assertSame(trainerStorage, trainerResult);
        assertSame(trainingStorage, trainingResult);
        assertSame(trainingTypeStorage, trainingTypeResult);
    }

    @Test
    void entityNameEnum_ShouldHaveAllRequiredValues() {
        InMemoryStorage.EntityName[] values = InMemoryStorage.EntityName.values();

        assertEquals(4, values.length);
        assertEquals(InMemoryStorage.EntityName.TRAINEE, InMemoryStorage.EntityName.valueOf("TRAINEE"));
        assertEquals(InMemoryStorage.EntityName.TRAINER, InMemoryStorage.EntityName.valueOf("TRAINER"));
        assertEquals(InMemoryStorage.EntityName.TRAINING, InMemoryStorage.EntityName.valueOf("TRAINING"));
        assertEquals(InMemoryStorage.EntityName.TRAINING_TYPE, InMemoryStorage.EntityName.valueOf("TRAINING_TYPE"));
    }
}
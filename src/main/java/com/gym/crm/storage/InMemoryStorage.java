package com.gym.crm.storage;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.gym.crm.storage.InMemoryStorage.EntityName.TRAINEE;
import static com.gym.crm.storage.InMemoryStorage.EntityName.TRAINER;
import static com.gym.crm.storage.InMemoryStorage.EntityName.TRAINING;
import static com.gym.crm.storage.InMemoryStorage.EntityName.TRAINING_TYPE;

@Component
@Getter
public class InMemoryStorage {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);

    enum EntityName {
        TRAINEE, TRAINER, TRAINING, TRAINING_TYPE
    }

    private Map<EntityName, Object> storages = new HashMap<>();

    @Autowired
    public void setTraineeStorage(TraineeStorage traineeStorage) {
        this.storages.put(TRAINEE, traineeStorage);
    }

    @Autowired
    public void setTrainerStorage(TrainerStorage trainerStorage) {
        this.storages.put(TRAINER, trainerStorage);
    }

    @Autowired
    public void setTrainingStorage(TrainingStorage trainingStorage) {
        this.storages.put(TRAINING, trainingStorage);
    }

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.storages.put(TRAINING_TYPE, trainingTypeStorage);
    }

    public TraineeStorage getTraineeStorage() {
        return getStorage(TRAINEE, TraineeStorage.class);
    }

    public TrainerStorage getTrainerStorage() {
        return getStorage(TRAINER, TrainerStorage.class);
    }

    public TrainingStorage getTrainingStorage() {
        return getStorage(TRAINING, TrainingStorage.class);
    }

    public TrainingTypeStorage getTrainingTypeStorage() {
        return getStorage(TRAINING_TYPE, TrainingTypeStorage.class);
    }

    private <T> T getStorage(EntityName entityName, Class<T> storageType) {
        Object obj = storages.get(entityName);

        if (storageType.isInstance(obj)) {
            return storageType.cast(obj);
        }

        logger.error("Storage type mismatch for entity: {}. Expected: {}", entityName, storageType.getSimpleName());
        throw new IllegalStateException("Invalid storage type for '%s'".formatted(entityName.name().toLowerCase()));
    }
}

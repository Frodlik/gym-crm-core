package com.gym.crm.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
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
    enum EntityName {
        TRAINEE, TRAINER, TRAINING, TRAINING_TYPE
    }

    private DataFileLoader dataFileLoader;
    private Map<EntityName, Object> storages = new HashMap<>();

    @Autowired
    public void setDataFileLoader(DataFileLoader dataFileLoader) {
        this.dataFileLoader = dataFileLoader;
    }

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

        throw new IllegalStateException("Invalid storage type for '%s'".formatted(entityName.name().toLowerCase()));
    }

    @PostConstruct
    public void initializeData() {
        TrainingTypeStorage trainingTypeStorage = getTrainingTypeStorage();
        TraineeStorage traineeStorage = getTraineeStorage();
        TrainerStorage trainerStorage = getTrainerStorage();
        TrainingStorage trainingStorage = getTrainingStorage();

        dataFileLoader.loadTrainingTypesFromFile(trainingTypeStorage);
        dataFileLoader.loadTraineesFromFile(traineeStorage);
        dataFileLoader.loadTrainersFromFile(trainerStorage, trainingTypeStorage);
        dataFileLoader.loadTrainingsFromFile(trainingStorage, trainingTypeStorage);
    }

}

package com.gym.crm.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class InMemoryStorage {
    public enum EntityName {
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
        this.storages.put(EntityName.TRAINEE, traineeStorage);
    }

    @Autowired
    public void setTrainerStorage(TrainerStorage trainerStorage) {
        this.storages.put(EntityName.TRAINER, trainerStorage);
    }

    @Autowired
    public void setTrainingStorage(TrainingStorage trainingStorage) {
        this.storages.put(EntityName.TRAINING, trainingStorage);
    }

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.storages.put(EntityName.TRAINING_TYPE, trainingTypeStorage);
    }

    public TraineeStorage getTraineeStorage() {
        Object obj = storages.get(EntityName.TRAINEE);
        if (obj instanceof TraineeStorage traineeStorage) {
            return traineeStorage;
        } else {
            throw new IllegalStateException("Invalid storage type for 'trainee'");
        }
    }

    public TrainerStorage getTrainerStorage() {
        Object obj = storages.get(EntityName.TRAINER);
        if (obj instanceof TrainerStorage trainerStorage) {
            return trainerStorage;
        } else {
            throw new IllegalStateException("Invalid storage type for 'trainer'");
        }
    }

    public TrainingStorage getTrainingStorage() {
        Object obj = storages.get(EntityName.TRAINING);
        if (obj instanceof TrainingStorage trainingStorage) {
            return trainingStorage;
        } else {
            throw new IllegalStateException("Invalid storage type for 'training'");
        }
    }

    public TrainingTypeStorage getTrainingTypeStorage() {
        Object obj = storages.get(EntityName.TRAINING_TYPE);
        if (obj instanceof TrainingTypeStorage trainingTypeStorage) {
            return trainingTypeStorage;
        } else {
            throw new IllegalStateException("Invalid storage type for 'training_type'");
        }
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

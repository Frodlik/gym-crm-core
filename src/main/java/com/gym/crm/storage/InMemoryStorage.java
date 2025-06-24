package com.gym.crm.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InMemoryStorage {
    @Value("${storage.init.file.path}")
    private String initDataFilePath;

    private DataFileLoader dataFileLoader;
    private TraineeStorage traineeStorage;
    private TrainerStorage trainerStorage;
    private TrainingStorage trainingStorage;
    private TrainingTypeStorage trainingTypeStorage;

    @Autowired
    public void setDataFileLoader(DataFileLoader dataFileLoader) {
        this.dataFileLoader = dataFileLoader;
    }

    @Autowired
    public void setTraineeStorage(TraineeStorage traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    @Autowired
    public void setTrainerStorage(TrainerStorage trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Autowired
    public void setTrainingStorage(TrainingStorage trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.trainingTypeStorage = trainingTypeStorage;
    }

    @PostConstruct
    public void initializeData() {
        dataFileLoader.loadTrainingTypesFromFile(initDataFilePath);
        dataFileLoader.loadTraineesFromFile(initDataFilePath);
        dataFileLoader.loadTrainersFromFile(initDataFilePath);
        dataFileLoader.loadTrainingsFromFile(initDataFilePath);
    }

}

package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class DataFileLoader {
    private TraineeStorage traineeStorage;
    private TrainerStorage trainerStorage;
    private TrainingStorage trainingStorage;
    private TrainingTypeStorage trainingTypeStorage;

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

    public void loadDataFromFile(String filePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    continue;
                }

                switch (currentSection) {
                    case "TRAINING_TYPES":
                        loadTrainingType(line);
                        break;
                    case "TRAINEES":
                        loadTrainee(line);
                        break;
                    case "TRAINERS":
                        loadTrainer(line);
                        break;
                    case "TRAININGS":
                        loadTraining(line);
                        break;
                }
            }
        } catch (IOException e) {
            log.warn("Could not load initial data from file: {}", filePath, e);
        }
    }

    private void loadTrainingType(String line) {
        String[] parts = line.split(",");
        if (parts.length < 1) {
            return;
        }

        String typeName = parts[0].trim();
        trainingTypeStorage.getTrainingTypes().put(typeName, new TrainingType(typeName));
    }

    private void loadTrainee(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return;
        }

        Long id = traineeStorage.getNextId();
        Trainee trainee = new Trainee();
        trainee.setUserId(id);
        trainee.setFirstName(parts[0].trim());
        trainee.setLastName(parts[1].trim());
        trainee.setUsername(parts[2].trim());
        trainee.setPassword(parts[3].trim());
        trainee.setIsActive(Boolean.parseBoolean(parts[4].trim()));
        trainee.setDateOfBirth(LocalDate.parse(parts[5].trim(), DateTimeFormatter.ISO_LOCAL_DATE));

        if (parts.length > 6 && !parts[6].trim().isEmpty()) {
            trainee.setAddress(parts[6].trim());
        }

        traineeStorage.getTrainees().put(id, trainee);
    }

    private void loadTrainer(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return;
        }

        Long id = trainerStorage.getNextId();
        Trainer trainer = new Trainer();
        trainer.setUserId(id);
        trainer.setFirstName(parts[0].trim());
        trainer.setLastName(parts[1].trim());
        trainer.setUsername(parts[2].trim());
        trainer.setPassword(parts[3].trim());
        trainer.setIsActive(Boolean.parseBoolean(parts[4].trim()));

        String specializationName = parts[5].trim();
        trainer.setSpecialization(trainingTypeStorage.getTrainingTypes().get(specializationName));

        trainerStorage.getTrainers().put(id, trainer);
    }

    private void loadTraining(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return;
        }

        Long id = trainingStorage.getNextId();
        Training training = new Training();
        training.setTraineeId(Long.parseLong(parts[0].trim()));
        training.setTrainerId(Long.parseLong(parts[1].trim()));
        training.setTrainingName(parts[2].trim());

        String trainingTypeName = parts[3].trim();
        training.setTrainingType(trainingTypeStorage.getTrainingTypes().get(trainingTypeName));
        training.setTrainingDate(LocalDate.parse(parts[4].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
        training.setDuration(Integer.parseInt(parts[5].trim()));

        trainingStorage.getTrainings().put(id, training);
    }
}

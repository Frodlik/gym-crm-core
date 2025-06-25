package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Component
@Slf4j
public class DataFileLoader {
    @Value("${storage.init.file.path}")
    private String initDataFilePath;

    private enum DataSection {
        TRAINING_TYPES, TRAINEES, TRAINERS, TRAININGS
    }

    public TrainingTypeStorage loadTrainingTypesFromFile(TrainingTypeStorage storage) {
        loadDataSection(DataSection.TRAINING_TYPES, line -> {
            TrainingType type = parseTrainingType(line);
            if (type != null) {
                storage.getTrainingTypes().put(type.getTrainingTypeName(), type);
            }
        });
        return storage;
    }

    public TraineeStorage loadTraineesFromFile(TraineeStorage storage) {
        loadDataSection(DataSection.TRAINEES, line -> {
            Trainee trainee = parseTrainee(line, storage.getNextId());
            if (trainee != null) {
                storage.getTrainees().put(trainee.getUserId(), trainee);
            }
        });
        return storage;
    }

    public TrainerStorage loadTrainersFromFile(TrainerStorage storage, TrainingTypeStorage typeStorage) {
        loadDataSection(DataSection.TRAINERS, line -> {
            Trainer trainer = parseTrainer(line, storage.getNextId(), typeStorage);
            if (trainer != null) {
                storage.getTrainers().put(trainer.getUserId(), trainer);
            }
        });
        return storage;
    }

    public TrainingStorage loadTrainingsFromFile(TrainingStorage storage, TrainingTypeStorage typeStorage) {
        loadDataSection(DataSection.TRAININGS, line -> {
            Training training = parseTraining(line, storage.getNextId(), typeStorage);
            if (training != null) {
                storage.getTrainings().put(storage.getNextId(), training);
            }
        });
        return storage;
    }

    private void loadDataSection(DataSection targetSection, Consumer<String> lineProcessor) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(initDataFilePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            boolean inTargetSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    String sectionName = line.substring(1, line.length() - 1);
                    inTargetSection = isInTargetSection(sectionName, targetSection);
                    continue;
                }

                if (inTargetSection) {
                    lineProcessor.accept(line);
                }
            }
        } catch (IOException e) {
            log.warn("Could not load {} data from file: {}", targetSection, initDataFilePath, e);
        }
    }

    private boolean isInTargetSection(String sectionName, DataSection targetSection) {
        try {
            return DataSection.valueOf(sectionName) == targetSection;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private TrainingType parseTrainingType(String line) {
        String[] parts = line.split(",");
        if (parts.length < 1) {
            return null;
        }
        return new TrainingType(parts[0].trim());
    }

    private Trainee parseTrainee(String line, Long id) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return null;
        }

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

        return trainee;
    }

    private Trainer parseTrainer(String line, Long id, TrainingTypeStorage typeStorage) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return null;
        }

        Trainer trainer = new Trainer();
        trainer.setUserId(id);
        trainer.setFirstName(parts[0].trim());
        trainer.setLastName(parts[1].trim());
        trainer.setUsername(parts[2].trim());
        trainer.setPassword(parts[3].trim());
        trainer.setIsActive(Boolean.parseBoolean(parts[4].trim()));

        String specializationName = parts[5].trim();
        trainer.setSpecialization(typeStorage.getTrainingTypes().get(specializationName));

        return trainer;
    }

    private Training parseTraining(String line, Long id, TrainingTypeStorage typeStorage) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            return null;
        }

        Training training = new Training();
        training.setTraineeId(Long.parseLong(parts[0].trim()));
        training.setTrainerId(Long.parseLong(parts[1].trim()));
        training.setTrainingName(parts[2].trim());

        String trainingTypeName = parts[3].trim();
        training.setTrainingType(typeStorage.getTrainingTypes().get(trainingTypeName));
        training.setTrainingDate(LocalDate.parse(parts[4].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
        training.setDuration(Integer.parseInt(parts[5].trim()));

        return training;
    }
}

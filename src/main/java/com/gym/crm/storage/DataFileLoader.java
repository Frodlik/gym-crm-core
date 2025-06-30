package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import static com.gym.crm.model.Trainee.*;
import static com.gym.crm.storage.DataFileLoader.DataSection.TRAINEES;
import static com.gym.crm.storage.DataFileLoader.DataSection.TRAINERS;
import static com.gym.crm.storage.DataFileLoader.DataSection.TRAININGS;
import static com.gym.crm.storage.DataFileLoader.DataSection.TRAINING_TYPES;

@Component
public class DataFileLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataFileLoader.class);

    @Value("${storage.init.file.path}")
    private String initDataFilePath;

    enum DataSection {
        TRAINING_TYPES, TRAINEES, TRAINERS, TRAININGS
    }

    public TrainingTypeStorage loadTrainingTypesFromFile(TrainingTypeStorage storage) {
        loadDataSection(TRAINING_TYPES, line -> {
            TrainingType type = parseTrainingType(line);
            if (type != null) {
                storage.getTrainingTypes().put(type.getTrainingTypeName(), type);
            }
        });
        return storage;
    }

    public TraineeStorage loadTraineesFromFile(TraineeStorage storage) {
        loadDataSection(TRAINEES, line -> {
            Trainee trainee = parseTrainee(line, storage.getNextId());
            if (trainee != null) {
                storage.getTrainees().put(trainee.getUserId(), trainee);
            }
        });
        return storage;
    }

    public TrainerStorage loadTrainersFromFile(TrainerStorage storage, TrainingTypeStorage typeStorage) {
        loadDataSection(TRAINERS, line -> {
            Trainer trainer = parseTrainer(line, storage.getNextId(), typeStorage);
            if (trainer != null) {
                storage.getTrainers().put(trainer.getUserId(), trainer);
            }
        });
        return storage;
    }

    public TrainingStorage loadTrainingsFromFile(TrainingStorage storage, TrainingTypeStorage typeStorage) {
        loadDataSection(TRAININGS, line -> {
            Long id = storage.getNextId();
            Training training = parseTraining(line, id, typeStorage);
            if (training != null) {
                storage.getTrainings().put(id, training);
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
            logger.warn("Could not load {} data from file: {}", targetSection, initDataFilePath, e);
        }
    }

    private boolean isInTargetSection(String sectionName, DataSection targetSection) {
        try {
            return DataSection.valueOf(sectionName) == targetSection;
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown section [{}] encountered in file, skipping.", sectionName);
            return false;
        }
    }

    private TrainingType parseTrainingType(String line) {
        String[] parts = line.split(",");
        if (parts.length < 1) {
            return null;
        }
        return TrainingType.builder()
                .trainingTypeName(parts[0].trim())
                .build();
    }

    private Trainee parseTrainee(String line, Long id) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            logger.warn("Invalid format for trainee line: {}", line);
            return null;
        }

        TraineeBuilder builder = builder()
                .userId(id)
                .firstName(parts[0].trim())
                .lastName(parts[1].trim())
                .username(parts[2].trim())
                .password(parts[3].trim())
                .isActive(Boolean.parseBoolean(parts[4].trim()))
                .dateOfBirth(LocalDate.parse(parts[5].trim(), DateTimeFormatter.ISO_LOCAL_DATE));

        if (parts.length > 6 && !parts[6].trim().isEmpty()) {
            builder.address(parts[6].trim());
        }

        return builder.build();
    }

    private Trainer parseTrainer(String line, Long id, TrainingTypeStorage typeStorage) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            logger.warn("Invalid format for trainer line: {}", line);
            return null;
        }

        String specializationName = parts[5].trim();
        TrainingType specialization = typeStorage.getTrainingTypes().get(specializationName);

        return Trainer.builder()
                .userId(id)
                .firstName(parts[0].trim())
                .lastName(parts[1].trim())
                .username(parts[2].trim())
                .password(parts[3].trim())
                .isActive(Boolean.parseBoolean(parts[4].trim()))
                .specialization(specialization)
                .build();
    }

    private Training parseTraining(String line, Long id, TrainingTypeStorage typeStorage) {
        String[] parts = line.split(",");
        if (parts.length < 6) {
            logger.warn("Invalid format for training line: {}", line);
            return null;
        }

        String trainingTypeName = parts[3].trim();
        TrainingType trainingType = typeStorage.getTrainingTypes().get(trainingTypeName);

        return Training.builder()
                .traineeId(Long.parseLong(parts[0].trim()))
                .trainerId(Long.parseLong(parts[1].trim()))
                .trainingName(parts[2].trim())
                .trainingType(trainingType)
                .trainingDate(LocalDate.parse(parts[4].trim(), DateTimeFormatter.ISO_LOCAL_DATE))
                .duration(Integer.parseInt(parts[5].trim()))
                .build();
    }
}

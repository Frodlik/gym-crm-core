package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Getter
public class InMemoryStorage {
    @Value("${storage.init.file.path}")
    private String initDataFilePath;

    private final Map<Long, Trainee> trainees = new ConcurrentHashMap<>();
    private final Map<Long, Trainer> trainers = new ConcurrentHashMap<>();
    private final Map<Long, Training> trainings = new ConcurrentHashMap<>();
    private final Map<String, TrainingType> trainingTypes = new ConcurrentHashMap<>();

    private final AtomicLong traineeIdGenerator = new AtomicLong(1);
    private final AtomicLong trainerIdGenerator = new AtomicLong(1);
    private final AtomicLong trainingIdGenerator = new AtomicLong(1);

    @PostConstruct
    public void initializeStorage() {
        loadInitialData();
    }

    private void loadInitialData() {
        try (InputStream inputStream = new FileInputStream(initDataFilePath) {
        };
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            if (inputStream == null) {
                loadDefaultData();
                return;
            }

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
            loadDefaultData();
        }
    }

    private void loadTrainingType(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 1) {
            String typeName = parts[0].trim();
            trainingTypes.put(typeName, new TrainingType(typeName));
        }
    }

    private void loadTrainee(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            Long id = traineeIdGenerator.getAndIncrement();
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
            trainees.put(id, trainee);
        }
    }

    private void loadTrainer(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            Long id = trainerIdGenerator.getAndIncrement();
            Trainer trainer = new Trainer();
            trainer.setUserId(id);
            trainer.setFirstName(parts[0].trim());
            trainer.setLastName(parts[1].trim());
            trainer.setUsername(parts[2].trim());
            trainer.setPassword(parts[3].trim());
            trainer.setIsActive(Boolean.parseBoolean(parts[4].trim()));
            String specializationName = parts[5].trim();
            trainer.setSpecialization(trainingTypes.get(specializationName));
            trainers.put(id, trainer);
        }
    }

    private void loadTraining(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            Long id = trainingIdGenerator.getAndIncrement();
            Training training = new Training();
            training.setTraineeId(Long.parseLong(parts[0].trim()));
            training.setTrainerId(Long.parseLong(parts[1].trim()));
            training.setTrainingName(parts[2].trim());
            String trainingTypeName = parts[3].trim();
            training.setTrainingType(trainingTypes.get(trainingTypeName));
            training.setTrainingDate(LocalDate.parse(parts[4].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
            training.setDuration(Integer.parseInt(parts[5].trim()));
            trainings.put(id, training);
        }
    }

    private void loadDefaultData() {
        trainingTypes.put("FITNESS", new TrainingType("FITNESS"));
        trainingTypes.put("YOGA", new TrainingType("YOGA"));
        trainingTypes.put("ZUMBA", new TrainingType("ZUMBA"));
        trainingTypes.put("STRETCHING", new TrainingType("STRETCHING"));
        trainingTypes.put("RESISTANCE", new TrainingType("RESISTANCE"));
    }

    public Long getNextTraineeId() {
        return traineeIdGenerator.getAndIncrement();
    }

    public Long getNextTrainerId() {
        return trainerIdGenerator.getAndIncrement();
    }

    public Long getNextTrainingId() {
        return trainingIdGenerator.getAndIncrement();
    }
}

package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TrainingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainingDAOImpl.class);

    private TrainingStorage trainingStorage;

    @Autowired
    public void setStorage(InMemoryStorage inMemoryStorage) {
        this.trainingStorage = inMemoryStorage.getTrainingStorage();
    }

    @Override
    public Training create(Training training) {
        Long id = trainingStorage.getNextId();

        Map<Long, Training> trainings = trainingStorage.getTrainings();
        trainings.put(id, training);

        log.info("Created Training with ID: {}", id);

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Map<Long, Training> trainings = trainingStorage.getTrainings();
        Training training = trainings.get(id);

        log.debug("Training found with ID: {}", id);

        return Optional.ofNullable(training);
    }

    @Override
    public List<Training> findAll() {
        Map<Long, Training> trainings = trainingStorage.getTrainings();

        log.debug("Retrieved all trainings. Count: {}", trainings.size());

        return trainings.values().stream()
                .toList();
    }
}

package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TrainingStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
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

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Map<Long, Training> trainings = trainingStorage.getTrainings();
        Training training = trainings.get(id);

        return Optional.ofNullable(training);
    }

    @Override
    public List<Training> findAll() {
        Map<Long, Training> trainings = trainingStorage.getTrainings();

        return trainings.values().stream()
                .toList();
    }
}

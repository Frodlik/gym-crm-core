package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TraineeStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDAOImpl implements TraineeDAO {
    private TraineeStorage traineeStorage;

    @Autowired
    public void setStorage(InMemoryStorage inMemoryStorage) {
        this.traineeStorage = inMemoryStorage.getTraineeStorage();
    }

    @Override
    public Trainee create(Trainee trainee) {
        Long id = traineeStorage.getNextId();
        trainee.setUserId(id);

        Map<Long, Trainee> trainees = traineeStorage.getTrainees();
        trainees.put(id, trainee);

        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();
        Trainee trainee = trainees.get(id);

        return Optional.ofNullable(trainee);
    }

    @Override
    public List<Trainee> findAll() {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();

        return trainees.values().stream()
                .toList();
    }

    @Override
    public Trainee update(Trainee trainee) {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();

        if (!trainees.containsKey(trainee.getUserId())) {
            throw new IllegalArgumentException("Trainee not found with ID: " + trainee.getUserId());
        }

        trainees.put(trainee.getUserId(), trainee);

        return trainee;
    }

    @Override
    public boolean delete(Long id) {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();
        Trainee removed = trainees.remove(id);

        return removed != null;
    }
}

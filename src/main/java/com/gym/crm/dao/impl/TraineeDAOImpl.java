package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainee;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TraineeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDAOImpl implements TraineeDAO {
    private static final Logger log = LoggerFactory.getLogger(TraineeDAOImpl.class);

    private TraineeStorage traineeStorage;

    @Autowired
    public void setStorage(InMemoryStorage inMemoryStorage) {
        this.traineeStorage = inMemoryStorage.getTraineeStorage();
    }

    @Override
    public Trainee create(Trainee trainee) {
        Long id = traineeStorage.getNextId();

        Trainee created = trainee.toBuilder()
                .id(id)
                .build();

        Map<Long, Trainee> trainees = traineeStorage.getTrainees();
        trainees.put(id, created);

        log.info("Created Trainee with id: {}", id);

        return created;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();
        Trainee trainee = trainees.get(id);

        log.debug("Found trainee with ID: {}", id);

        return Optional.ofNullable(trainee);
    }

    @Override
    public List<Trainee> findAll() {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();

        log.debug("Retrieved all trainees. Count: {}", trainees.size());

        return trainees.values().stream()
                .toList();
    }

    @Override
    public Trainee update(Trainee trainee) {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();

        if (!trainees.containsKey(trainee.getId())) {
            throw new DaoException("Trainee not found with ID: " + trainee.getId());
        }

        trainees.put(trainee.getId(), trainee);

        log.info("Trainee updated with ID: {}", trainee.getId());

        return trainee;
    }

    @Override
    public boolean delete(Long id) {
        Map<Long, Trainee> trainees = traineeStorage.getTrainees();
        Trainee removed = trainees.remove(id);

        log.info("Trainee deleted with ID: {}", id);

        return removed != null;
    }
}

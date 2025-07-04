package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.InMemoryStorage;
import com.gym.crm.storage.TrainerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDAOImpl implements TrainerDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainerDAOImpl.class);

    private TrainerStorage trainerStorage;

    @Autowired
    public void setStorage(InMemoryStorage inMemoryStorage) {
        this.trainerStorage = inMemoryStorage.getTrainerStorage();
    }

    @Override
    public Trainer create(Trainer trainer) {
        Long id = trainerStorage.getNextId();

        Trainer created = trainer.toBuilder()
                .id(id)
                .build();

        Map<Long, Trainer> trainers = trainerStorage.getTrainers();
        trainers.put(id, created);

        log.info("Created Trainer with ID: {}", id);

        return created;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Map<Long, Trainer> trainers = trainerStorage.getTrainers();
        Trainer trainer = trainers.get(id);

        log.debug("Trainer found with ID: {}", id);

        return Optional.ofNullable(trainer);
    }

    @Override
    public List<Trainer> findAll() {
        Map<Long, Trainer> trainers = trainerStorage.getTrainers();

        log.debug("Retrieved all trainers. Count: {}", trainers.size());

        return trainers.values().stream()
                .toList();
    }

    @Override
    public Trainer update(Trainer trainer) {
        Map<Long, Trainer> trainers = trainerStorage.getTrainers();

        if (!trainers.containsKey(trainer.getId())) {
            throw new DaoException("Trainer not found with ID: " + trainer.getId());
        }

        trainers.put(trainer.getId(), trainer);

        log.info("Trainer updated with ID: {}", trainer.getId());

        return trainer;
    }
}

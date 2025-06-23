package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDAOImpl implements TrainerDAO {
    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    @Override
    public Trainer create(Trainer trainer) {
        Long id = storage.getNextTrainerId();
        trainer.setUserId(id);

        Map<Long, Trainer> trainers = storage.getTrainers();
        trainers.put(id, trainer);

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Map<Long, Trainer> trainers = storage.getTrainers();
        Trainer trainer = trainers.get(id);

        return Optional.ofNullable(trainer);
    }

    @Override
    public List<Trainer> findAll() {
        Map<Long, Trainer> trainers = storage.getTrainers();

        return trainers.values().stream()
                .toList();
    }

    @Override
    public Trainer update(Trainer trainer) {
        Map<Long, Trainer> trainers = storage.getTrainers();

        if (!trainers.containsKey(trainer.getUserId())) {
            throw new IllegalArgumentException("Trainer not found with ID: " + trainer.getUserId());
        }

        trainers.put(trainer.getUserId(), trainer);

        return trainer;
    }
}

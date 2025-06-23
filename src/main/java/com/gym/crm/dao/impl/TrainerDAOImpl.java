package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.TrainerStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDAOImpl implements TrainerDAO {
    private TrainerStorage trainerStorage;

    @Autowired
    public void setStorage(TrainerStorage trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Override
    public Trainer create(Trainer trainer) {
        Long id = trainerStorage.getNextId();
        trainer.setUserId(id);

        Map<Long, Trainer> trainers = trainerStorage.getTrainers();
        trainers.put(id, trainer);

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Map<Long, Trainer> trainers = trainerStorage.getTrainers();
        Trainer trainer = trainers.get(id);

        return Optional.ofNullable(trainer);
    }

    @Override
    public List<Trainer> findAll() {
        Map<Long, Trainer> trainers = trainerStorage.getTrainers();

        return trainers.values().stream()
                .toList();
    }

    @Override
    public Trainer update(Trainer trainer) {
        Map<Long, Trainer> trainers = trainerStorage.getTrainers();

        if (!trainers.containsKey(trainer.getUserId())) {
            throw new IllegalArgumentException("Trainer not found with ID: " + trainer.getUserId());
        }

        trainers.put(trainer.getUserId(), trainer);

        return trainer;
    }
}

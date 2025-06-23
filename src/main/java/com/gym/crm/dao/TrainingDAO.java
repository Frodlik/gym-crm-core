package com.gym.crm.dao;

import com.gym.crm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDAO {
    Training create(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}

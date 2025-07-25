package com.gym.crm.dao;

import com.gym.crm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDAO {
    Optional<TrainingType> findByName(String name);

    List<TrainingType> findAll();
}

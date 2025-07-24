package com.gym.crm.dao;

import com.gym.crm.model.TrainingType;

import java.util.Optional;

public interface TrainingTypeDAO {
    Optional<TrainingType> getByName(String name);
}

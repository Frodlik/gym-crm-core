package com.gym.crm.service;

import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;

import java.util.Optional;

public interface TrainingService {
    TrainingResponse create(TrainingCreateRequest training);

    Optional<TrainingResponse> findById(Long id);
}

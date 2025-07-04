package com.gym.crm.service;

import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;

import java.util.Optional;

public interface TrainerService {
    TrainerResponse create(TrainerCreateRequest request);

    Optional<TrainerResponse> findById(Long id);

    TrainerResponse update(TrainerUpdateRequest request);
}

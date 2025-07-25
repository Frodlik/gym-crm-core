package com.gym.crm.service;

import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.dto.training.TrainingCreateRequestDto;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingService {
    void create(TrainingCreateRequestDto training);

    Optional<TrainingResponse> findById(Long id);

    List<TrainingResponse> getTraineeTrainingsByCriteria(TraineeSearchFilter filter);

    List<TrainingResponse> getTrainerTrainingsByCriteria(TrainerSearchFilter filter);

    List<TrainingType> getAllTrainingTypes();
}

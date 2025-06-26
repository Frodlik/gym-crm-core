package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private TrainingDAO trainingDAO;
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private TrainingMapper trainingMapper;

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTrainingMapper(TrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Override
    public TrainingResponse create(TrainingCreateRequest request) {
        Optional<Trainee> trainee = traineeDAO.findById(request.getTraineeId());
        Optional<Trainer> trainer = trainerDAO.findById(request.getTrainerId());

        if (trainee.isEmpty() || trainer.isEmpty()) {
            throw new CoreServiceException("Trainee or/and Trainer was not found");
        }

        Training training = trainingMapper.toEntity(request);
        training.setTraineeId(trainee.get().getUserId());
        training.setTrainerId(trainer.get().getUserId());

        Training saved = trainingDAO.create(training);

        return trainingMapper.toResponse(saved);
    }

    @Override
    public Optional<TrainingResponse> findById(Long id) {
        return trainingDAO.findById(id)
                .map(trainingMapper::toResponse);
    }
}

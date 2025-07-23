package com.gym.crm.service.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.transaction.PersistenceTx;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

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
    @PersistenceTx
    public TrainingResponse create(@Valid TrainingCreateRequest request) {
        logger.debug("Creating training: traineeId={}, trainerId={}", request.getTraineeId(), request.getTrainerId());

        Optional<Trainee> trainee = traineeDAO.findById(request.getTraineeId());
        Optional<Trainer> trainer = trainerDAO.findById(request.getTrainerId());

        if (trainee.isEmpty() || trainer.isEmpty()) {
            throw new CoreServiceException("Trainee or/and Trainer was not found");
        }

        Training training = trainingMapper.toEntity(request);

        training = training.toBuilder()
                .trainee(trainee.get())
                .trainer(trainer.get())
                .build();

        Training saved = trainingDAO.create(training);

        logger.info("Training created successfully");

        return trainingMapper.toResponse(saved);
    }

    @Override
    public Optional<TrainingResponse> findById(Long id) {
        logger.debug("Finding training by ID: {}", id);

        return trainingDAO.findById(id)
                .map(trainingMapper::toResponse);
    }

    @Override
    @PersistenceTx(readOnly = true)
    public List<TrainingResponse> getTraineeTrainingsByCriteria(@Valid TraineeSearchFilter filter) {
        logger.debug("Getting trainee trainings by criteria: {}", filter);

        if (traineeDAO.findByUsername(filter.getTraineeUsername()).isEmpty()) {
            throw new CoreServiceException("Trainee not found with username: " + filter.getTraineeUsername());
        }

        List<Training> trainings = trainingDAO.findTraineeTrainingsByCriteria(
                filter.getTraineeUsername(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getTrainerName(),
                filter.getTrainingType()
        );

        logger.info("Found {} trainings for trainee: {}", trainings.size(), filter.getTraineeUsername());

        return trainings.stream()
                .map(trainingMapper::toResponse)
                .toList();
    }

    @Override
    @PersistenceTx(readOnly = true)
    public List<TrainingResponse> getTrainerTrainingsByCriteria(@Valid TrainerSearchFilter filter) {
        logger.debug("Getting trainer trainings by criteria: {}", filter);

        if (trainerDAO.findByUsername(filter.getTrainerUsername()).isEmpty()) {
            throw new CoreServiceException("Trainer not found with username: " + filter.getTrainerUsername());
        }

        List<Training> trainings = trainingDAO.findTrainerTrainingsByCriteria(
                filter.getTrainerUsername(),
                filter.getFromDate(),
                filter.getToDate(),
                filter.getTraineeName()
        );

        logger.info("Found {} trainings for trainer: {}", trainings.size(), filter.getTrainerUsername());

        return trainings.stream()
                .map(trainingMapper::toResponse)
                .toList();
    }
}

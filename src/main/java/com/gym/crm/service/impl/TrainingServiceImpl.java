package com.gym.crm.service.impl;

import com.gym.crm.actuator.prometheus.TrainingMetrics;
import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.dto.training.TrainingCreateRequestDto;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.exception.CoreServiceException;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.repository.specification.TrainingSpecifications;
import com.gym.crm.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingMetrics trainingMetrics;

    @Override
    @Transactional
    public void create(@Valid TrainingCreateRequestDto request) {
        logger.debug("Creating training: traineeUsername={}, trainerUsername={}", request.getTraineeUsername(), request.getTrainerUsername());

        Trainee trainee = traineeRepository.findTraineeByUser_Username(request.getTraineeUsername())
                .orElseThrow(() -> new CoreServiceException("Trainee not found with username: " + request.getTraineeUsername()));

        Trainer trainer = trainerRepository.findTrainerByUser_Username(request.getTrainerUsername())
                .orElseThrow(() -> new CoreServiceException("Trainer not found with username: " + request.getTrainerUsername()));

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(request.getTrainingName())
                .orElseThrow(() -> new CoreServiceException("Training type not found with name: " + request.getTrainingName()));

        Training training = trainingMapper.toEntity(request);
        training = training.toBuilder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .build();

        trainingRepository.save(training);
        trainingMetrics.recordTrainingCreated();
        logger.info("Training created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingResponse> findById(Long id) {
        logger.debug("Finding training by ID: {}", id);

        return trainingRepository.findById(id)
                .map(trainingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTraineeTrainingsByCriteria(@Valid TraineeSearchFilter filter) {
        logger.debug("Getting trainee trainings by criteria: {}", filter);

        if (traineeRepository.findTraineeByUser_Username(filter.getTraineeUsername()).isEmpty()) {
            throw new CoreServiceException("Trainee not found with username: " + filter.getTraineeUsername());
        }

        Specification<Training> spec = TrainingSpecifications.forTraineeSearch(filter);
        List<Training> trainings = trainingRepository.findAll(spec);

        logger.info("Found {} trainings for trainee: {}", trainings.size(), filter.getTraineeUsername());
        trainingMetrics.recordTrainingRetrieval();

        return trainings.stream()
                .map(trainingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainerTrainingsByCriteria(@Valid TrainerSearchFilter filter) {
        logger.debug("Getting trainer trainings by criteria: {}", filter);

        if (trainerRepository.findTrainerByUser_Username(filter.getTrainerUsername()).isEmpty()) {
            throw new CoreServiceException("Trainer not found with username: " + filter.getTrainerUsername());
        }

        Specification<Training> spec = TrainingSpecifications.forTrainerSearch(filter);
        List<Training> trainings = trainingRepository.findAll(spec);

        logger.info("Found {} trainings for trainer: {}", trainings.size(), filter.getTrainerUsername());
        trainingMetrics.recordTrainingRetrieval();

        return trainings.stream()
                .map(trainingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        logger.info("Retrieving all training types");

        return trainingTypeRepository.findAll();
    }
}

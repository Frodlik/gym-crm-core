package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainingCriteriaRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerTrainingCriteriaRequest;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.openapi.model.AvailableTrainerGetResponse;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gym.crm.openapi.model.TraineeCreateRequest;
import com.gym.crm.openapi.model.TraineeCreateResponse;
import com.gym.crm.openapi.model.TraineeGetResponse;
import com.gym.crm.openapi.model.TraineeTrainingGetResponse;
import com.gym.crm.openapi.model.TraineeUpdateRequest;
import com.gym.crm.openapi.model.TraineeUpdateResponse;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {
    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    public GymFacade(TraineeService traineeService, TrainerService trainerService,
                     TrainingService trainingService, TraineeMapper traineeMapper,
                     TrainerMapper trainerMapper, TrainingMapper trainingMapper) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.traineeMapper = traineeMapper;
        this.trainerMapper = trainerMapper;
        this.trainingMapper = trainingMapper;
    }

    public TraineeCreateResponse createTrainee(TraineeCreateRequest request) {
        logger.info("Facade: Creating trainee");

        TraineeCreateRequestDto createRequestDto = traineeMapper.toCreateRequest(request);

        return traineeMapper.toRestCreateResponse(traineeService.create(createRequestDto));
    }

    public TraineeGetResponse getTraineeByUsername(String targetUsername) {
        logger.debug("Facade: Getting trainee by username: {}", targetUsername);

        return traineeMapper.toRestGetResponse(traineeService.findByUsername(targetUsername));
    }

    public TraineeUpdateResponse updateTrainee(String username, TraineeUpdateRequest request) {
        logger.info("Facade: Updating trainee with username: {}", username);

        TraineeUpdateRequestDto requestDto = traineeMapper.toUpdateRequest(request);

        return traineeMapper.toRestUpdateResponse(traineeService.update(requestDto, username));
    }

    public TraineeAssignedTrainersUpdateResponse updateTraineeTrainersList(String username, TraineeAssignedTrainersUpdateRequest request) {
        logger.info("Facade: Updating trainers list for trainee with username: {}", username);

        TraineeTrainersUpdateRequestDto trainersUpdateRequest = traineeMapper.toTrainersUpdateRequest(request);

        return traineeMapper.toRestTrainersUpdateResponse(traineeService.updateTraineeTrainersList(trainersUpdateRequest, username));
    }

    public void deleteTrainee(String targetUsername) {
        logger.info("Facade: Deleting trainee with username: {}", targetUsername);

        traineeService.deleteByUsername(targetUsername);
    }

    public void changeTraineePassword(PasswordChangeRequest request) {
        logger.info("Facade: Changing password for trainee with username: {}", request.getUsername());

        traineeService.changePassword(request);
    }

    public void toggleTraineeActivation(String targetUsername, boolean isActive) {
        logger.info("Facade: Toggling activation for trainee with username: {}", targetUsername);

        traineeService.toggleTraineeActivation(targetUsername, isActive);
    }

    public TrainerResponse createTrainer(TrainerCreateRequest request) {
        logger.info("Facade: Creating trainer");

        return trainerService.create(request);
    }

    public Optional<TrainerResponse> getTrainerByUsername(String targetUsername) {
        logger.debug("Facade: Getting trainer by username: {}", targetUsername);

        return trainerService.findByUsername(targetUsername);
    }

    public List<AvailableTrainerGetResponse> getTrainersNotAssignedToTrainee(String traineeUsername) {
        logger.debug("Facade: Getting trainers not assigned to trainee with username: {}", traineeUsername);

        return trainerService.findTrainersNotAssignedToTrainee(traineeUsername).stream()
                .map(trainerMapper::toRestAvailableTrainerResponse)
                .toList();
    }

    public TrainerResponse updateTrainer(TrainerUpdateRequest request) {
        logger.info("Facade: Updating trainer with ID: {}", request.getId());

        return trainerService.update(request);
    }

    public void changeTrainerPassword(PasswordChangeRequest request) {
        logger.info("Facade: Changing password for trainer with username: {}", request.getUsername());
        trainerService.changePassword(request);
    }

    public TrainerResponse toggleTrainerActivation(String targetUsername) {
        logger.info("Facade: Toggling activation for trainer with username: {}", targetUsername);

        return trainerService.toggleTrainerActivation(targetUsername);
    }

    public TrainingResponse createTraining(TrainingCreateRequest training) {
        logger.info("Facade: Creating training");

        return trainingService.create(training);
    }

    public Optional<TrainingResponse> getTrainingById(Long id) {
        logger.debug("Facade: Getting training by ID: {}", id);

        return trainingService.findById(id);
    }

    public List<TraineeTrainingGetResponse> getTraineeTrainingsByCriteria(TraineeTrainingCriteriaRequestDto request) {
        logger.debug("Facade: Getting trainee trainings by criteria for username: {}", request.getTraineeUsername());

        List<TrainingResponse> responses = trainingService.getTraineeTrainingsByCriteria(
                request.getTraineeUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(),
                request.getTrainingType()
        );

        return responses.stream()
                .map(trainingMapper::toRestTrainingGetResponse)
                .toList();
    }

    public List<TrainingResponse> getTrainerTrainingsByCriteria(TrainerTrainingCriteriaRequest request) {
        logger.debug("Facade: Getting trainer trainings by criteria for username: {}", request.getTrainerUsername());

        return trainingService.getTrainerTrainingsByCriteria(
                request.getTrainerUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName()
        );
    }
}

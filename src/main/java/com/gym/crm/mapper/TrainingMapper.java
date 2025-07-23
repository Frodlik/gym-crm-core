package com.gym.crm.mapper;

import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.model.Training;
import com.gym.crm.openapi.model.TraineeTrainingGetResponse;
import com.gym.crm.openapi.model.TrainerTrainingGetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    Training toEntity(TrainingCreateRequest request);

    @Mapping(target = "trainerName", source = "trainer.user.firstName")
    @Mapping(target = "traineeName", source = "trainee.user.firstName")
    @Mapping(target = "trainingTypeName", source = "trainingType.trainingTypeName")
    TrainingResponse toResponse(Training training);

    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "trainerName", source = "trainerName")
    TraineeTrainingGetResponse toRestTraineeTrainingGetResponse(TrainingResponse response);

    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "traineeName", source = "traineeName")
    TrainerTrainingGetResponse toRestTrainerTrainingGetResponse(TrainingResponse response);
}
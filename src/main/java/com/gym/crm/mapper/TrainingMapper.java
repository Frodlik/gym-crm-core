package com.gym.crm.mapper;

import com.gym.crm.dto.training.TrainingCreateRequestDto;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.openapi.model.TraineeTrainingGetResponse;
import com.gym.crm.openapi.model.TrainerTrainingGetResponse;
import com.gym.crm.openapi.model.TrainingCreateRequest;
import com.gym.crm.openapi.model.TrainingTypeGetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    Training toEntity(TrainingCreateRequestDto request);

    @Mapping(target = "trainerName", source = "trainer.user.firstName")
    @Mapping(target = "traineeName", source = "trainee.user.firstName")
    @Mapping(target = "trainingTypeName", source = "trainingType.trainingTypeName")
    TrainingResponse toResponse(Training training);

    TrainingCreateRequestDto toCreateRequestDto(TrainingCreateRequest request);

    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "trainerName", source = "trainerName")
    TraineeTrainingGetResponse toRestTraineeTrainingGetResponse(TrainingResponse response);

    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "traineeName", source = "traineeName")
    TrainerTrainingGetResponse toRestTrainerTrainingGetResponse(TrainingResponse response);

    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "trainingTypeId", source = "id")
    TrainingTypeGetResponse toRestTrainingTypeGetResponse(TrainingType trainingType);

    default String map(TrainingType trainingType) {
        return trainingType != null ? trainingType.getTrainingTypeName() : null;
    }
}
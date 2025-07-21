package com.gym.crm.mapper;

import com.gym.crm.dto.model.TrainerModel;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.openapi.model.AvailableTrainerGetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {
    Trainer toEntity(TrainerCreateRequest request);

    Trainer toEntity(TrainerUpdateRequest request);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "specialization", source = "specialization")
    TrainerResponse toResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization")
    TrainerModel toTrainerModel(Trainer trainer);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "specialization", source = "specialization")
    AvailableTrainerGetResponse toRestAvailableTrainerResponse(TrainerResponse response);

    default String map(TrainingType trainingType) {
        return trainingType != null ? trainingType.getTrainingTypeName() : null;
    }
}

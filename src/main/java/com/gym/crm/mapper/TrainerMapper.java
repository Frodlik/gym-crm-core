package com.gym.crm.mapper;

import com.gym.crm.dto.model.TraineeModel;
import com.gym.crm.dto.trainer.AvailableTrainerResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateResponseDto;
import com.gym.crm.dto.trainer.TrainerGetResponseDto;
import com.gym.crm.dto.trainer.TrainerUpdateRequestDto;
import com.gym.crm.dto.trainer.TrainerUpdateResponseDto;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.openapi.model.AvailableTrainerGetResponse;
import com.gym.crm.openapi.model.TrainerCreateRequest;
import com.gym.crm.openapi.model.TrainerCreateResponse;
import com.gym.crm.openapi.model.TrainerGetResponse;
import com.gym.crm.openapi.model.TrainerUpdateRequest;
import com.gym.crm.openapi.model.TrainerUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {
    Trainer toEntity(TrainerCreateRequestDto request);

    Trainer toEntity(TrainerUpdateRequestDto request);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "trainees", source = "trainees")
    TrainerGetResponseDto toResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    TraineeModel toTraineeModel(Trainee trainee);

    @Mapping(target = "specialization.trainingTypeName", source = "specialization")
    TrainerCreateRequestDto toCreateRequestDto(TrainerCreateRequest request);

    @Mapping(target = "specialization.trainingTypeName", source = "specialization")
    TrainerUpdateRequestDto toUpdateRequestDto(TrainerUpdateRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainees", source = "trainees")
    TrainerUpdateResponseDto toUpdateResponseDto(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    AvailableTrainerResponseDto toAvailableTrainerResponseDto(Trainer trainer);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "specialization", source = "specialization")
    AvailableTrainerGetResponse toRestAvailableTrainerResponse(AvailableTrainerResponseDto response);

    TrainerCreateResponse toRestCreateResponse(TrainerCreateResponseDto response);

    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "trainees", source = "trainees")
    TrainerGetResponse toRestGetResponse(TrainerGetResponseDto response);

    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "trainees", source = "trainees")
    TrainerUpdateResponse toRestUpdateResponse(TrainerUpdateResponseDto responseDto);

    default String map(TrainingType trainingType) {
        return trainingType != null ? trainingType.getTrainingTypeName() : null;
    }
}

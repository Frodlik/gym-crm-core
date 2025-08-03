package com.gym.crm.mapper;

import com.gym.crm.dto.model.TrainerModel;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gym.crm.openapi.model.TraineeCreateRequest;
import com.gym.crm.openapi.model.TraineeCreateResponse;
import com.gym.crm.openapi.model.TraineeGetResponse;
import com.gym.crm.openapi.model.TraineeUpdateRequest;
import com.gym.crm.openapi.model.TraineeUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TraineeMapper {
    Trainee toEntity(TraineeCreateRequestDto request);

    Trainee toEntity(TraineeUpdateRequestDto request);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainers", source = "trainers")
    TraineeGetResponseDto toResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization")
    TrainerModel toTrainerModel(Trainer trainer);

    TraineeCreateRequestDto toCreateRequest(TraineeCreateRequest request);

    TraineeUpdateRequestDto toUpdateRequest(TraineeUpdateRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainers", source = "trainers")
    TraineeUpdateResponseDto toUpdateResponseDto(Trainee trainee);

    TraineeTrainersUpdateRequestDto toTrainersUpdateRequest(TraineeAssignedTrainersUpdateRequest request);

    TraineeTrainersUpdateResponseDto toTrainersUpdateResponse(Trainee trainee);

    @Mapping(target = "isActive", source = "active")
    TraineeGetResponse toRestGetResponse(TraineeGetResponseDto response);

    TraineeCreateResponse toRestCreateResponse(TraineeCreateResponseDto responseDto);

    @Mapping(target = "isActive", source = "active")
    TraineeUpdateResponse toRestUpdateResponse(TraineeUpdateResponseDto responseDto);

    @Mapping(target = "trainers", source = "trainers")
    TraineeAssignedTrainersUpdateResponse toRestTrainersUpdateResponse(TraineeTrainersUpdateResponseDto dto);

    default String map(TrainingType trainingType) {
        return trainingType != null ? trainingType.getTrainingTypeName() : null;
    }
}

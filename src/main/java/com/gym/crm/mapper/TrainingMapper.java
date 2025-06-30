package com.gym.crm.mapper;

import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.model.Training;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    Training toEntity(TrainingCreateRequest request);

    TrainingResponse toResponse(Training training);
}
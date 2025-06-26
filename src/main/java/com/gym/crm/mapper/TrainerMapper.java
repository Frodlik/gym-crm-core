package com.gym.crm.mapper;

import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TrainerMapper {
    TrainerMapper INSTANCE = Mappers.getMapper(TrainerMapper.class);

    Trainer toEntity(TrainerCreateRequest request);

    Trainer toEntity(TrainerUpdateRequest request);

    TrainerResponse toResponse(Trainer trainer);
}

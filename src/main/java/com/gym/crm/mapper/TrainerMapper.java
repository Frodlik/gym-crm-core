package com.gym.crm.mapper;

import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.model.Trainer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainerMapper {
    Trainer toEntity(TrainerCreateRequest request);

    Trainer toEntity(TrainerUpdateRequest request);

    TrainerResponse toResponse(Trainer trainer);
}

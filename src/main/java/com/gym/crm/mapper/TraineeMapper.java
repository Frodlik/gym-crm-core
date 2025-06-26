package com.gym.crm.mapper;

import com.gym.crm.dto.trainee.TraineeCreateRequest;
import com.gym.crm.dto.trainee.TraineeResponse;
import com.gym.crm.dto.trainee.TraineeUpdateRequest;
import com.gym.crm.model.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TraineeMapper {
    TraineeMapper INSTANCE = Mappers.getMapper(TraineeMapper.class);

    Trainee toEntity(TraineeCreateRequest request);

    Trainee toEntity(TraineeUpdateRequest request);

    TraineeResponse toResponse(Trainee trainee);
}

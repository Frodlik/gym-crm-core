package com.gym.crm.service;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainer.AvailableTrainerResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateResponseDto;
import com.gym.crm.dto.trainer.TrainerGetResponseDto;
import com.gym.crm.dto.trainer.TrainerUpdateRequestDto;
import com.gym.crm.dto.trainer.TrainerUpdateResponseDto;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    TrainerCreateResponseDto create(@Valid TrainerCreateRequestDto request);

    Optional<TrainerGetResponseDto> findById(Long id);

    TrainerGetResponseDto findByUsername(String username);

    List<AvailableTrainerResponseDto> findTrainersNotAssignedToTrainee(String traineeUsername);

    TrainerUpdateResponseDto update(@Valid TrainerUpdateRequestDto request, String username);

    void changePassword(@Valid PasswordChangeRequest request);

    void toggleTrainerActivation(String username, boolean isActive);
}

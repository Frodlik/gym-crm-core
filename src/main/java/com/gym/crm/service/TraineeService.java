package com.gym.crm.service;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import jakarta.validation.Valid;

import java.util.Optional;

public interface TraineeService {
    TraineeCreateResponseDto create(@Valid TraineeCreateRequestDto request);

    Optional<TraineeGetResponseDto> findById(Long id);

    TraineeGetResponseDto findByUsername(String username);

    TraineeUpdateResponseDto update(@Valid TraineeUpdateRequestDto request, String username);

    TraineeTrainersUpdateResponseDto updateTraineeTrainersList(@Valid TraineeTrainersUpdateRequestDto request, String username);

    void deleteByUsername(String username);

    void changePassword(@Valid PasswordChangeRequest request);

    void toggleTraineeActivation(String username, boolean isActive);
}

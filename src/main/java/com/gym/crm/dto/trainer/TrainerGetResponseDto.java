package com.gym.crm.dto.trainer;

import com.gym.crm.dto.model.TraineeModel;
import com.gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainerGetResponseDto {
    private String firstName;
    private String lastName;
    private TrainingType specialization;
    private boolean isActive;
    private Set<TraineeModel> trainees;
}

package com.gym.crm.dto.trainer;

import com.gym.crm.dto.model.TraineeModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainerUpdateResponseDto {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private Set<TraineeModel> trainees;
}

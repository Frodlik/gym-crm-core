package com.gym.crm.dto.trainer;

import com.gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCreateRequest {
    private String firstName;
    private String lastName;
    private TrainingType specialization;
}

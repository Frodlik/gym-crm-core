package com.gym.crm.dto.trainer;

import com.gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private TrainingType specialization;
}

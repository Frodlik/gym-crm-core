package com.gym.crm.dto.trainer;

import com.gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainerUpdateRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private TrainingType specialization;
}

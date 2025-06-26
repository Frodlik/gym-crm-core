package com.gym.crm.dto.trainer;

import com.gym.crm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private boolean isActive;
    private TrainingType specialization;
}

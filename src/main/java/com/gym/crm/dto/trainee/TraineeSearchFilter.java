package com.gym.crm.dto.trainee;

import com.gym.crm.dto.EntitySearchFilter;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TraineeSearchFilter extends EntitySearchFilter {
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    private String trainerName;

    private String trainingType;
}

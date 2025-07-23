package com.gym.crm.dto.trainer;

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
public class TrainerSearchFilter extends EntitySearchFilter {
    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    private String traineeName;
}

package com.gym.crm.dto.trainer;

import com.gym.crm.dto.EntitySearchFilter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private static final String PATTERN_USERNAME = "^[a-zA-Z]+\\.[a-zA-Z]+$";

    @NotBlank(message = "Trainer username is required")
    @Pattern(
            regexp = PATTERN_USERNAME,
            message = "Trainer username must be in format 'firstname.lastname'"
    )
    private String trainerUsername;

    private String traineeName;
}

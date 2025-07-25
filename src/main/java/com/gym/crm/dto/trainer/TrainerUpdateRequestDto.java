package com.gym.crm.dto.trainer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gym.crm.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainerUpdateRequestDto {
    @NotBlank(message = "First name is required and cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required and cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotNull(message = "Specialization is required")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TrainingType specialization;

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}

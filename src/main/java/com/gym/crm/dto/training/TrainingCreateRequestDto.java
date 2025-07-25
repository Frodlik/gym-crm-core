package com.gym.crm.dto.training;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainingCreateRequestDto {
    private static final String PATTERN_USERNAME = "^[a-zA-Z]+\\.[a-zA-Z]+$";

    @NotBlank(message = "Trainee username is required")
    @Pattern(
            regexp = PATTERN_USERNAME,
            message = "Trainee username must be in format 'firstname.lastname'"
    )
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    @Pattern(
            regexp = PATTERN_USERNAME,
            message = "Trainer username must be in format 'firstname.lastname'"
    )
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    @Size(max = 100, message = "Training name must be at most 100 characters")
    private String trainingName;

    @NotNull(message = "Training date is required")
    @FutureOrPresent(message = "Training date must be today or in the future")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration;
}

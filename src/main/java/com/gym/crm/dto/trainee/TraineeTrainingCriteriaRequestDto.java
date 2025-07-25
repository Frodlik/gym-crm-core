package com.gym.crm.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeTrainingCriteriaRequestDto {
    private static final String PATTERN_USERNAME = "^[a-zA-Z]+\\.[a-zA-Z]+$";

    @NotBlank(message = "Trainee username is required")
    @Pattern(
            regexp = PATTERN_USERNAME,
            message = "Trainee username must be in the format firstname.lastname"
    )
    private String traineeUsername;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private String trainerName;
    private String trainingType;
}

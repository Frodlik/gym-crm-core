package com.gym.crm.dto.trainer;

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
public class TrainerTrainingCriteriaRequest {
    private static final String PATTERN_USERNAME = "^[a-zA-Z]+\\.[a-zA-Z]+$";

    @NotBlank(message = "Trainer username is required")
    @Pattern(
            regexp = PATTERN_USERNAME,
            message = "Trainer username must be in format 'firstname.lastname'"
    )
    private String trainerUsername;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private String traineeName;
}

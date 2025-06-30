package com.gym.crm.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TrainingCreateRequest {
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private LocalDate trainingDate;
    private Integer trainingDuration;
}

package com.gym.crm.dto.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCreateRequest {
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private LocalDate trainingDate;
    private Integer trainingDuration;
}

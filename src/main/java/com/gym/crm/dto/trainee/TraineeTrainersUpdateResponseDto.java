package com.gym.crm.dto.trainee;

import com.gym.crm.dto.model.TrainerModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeTrainersUpdateResponseDto {
    private List<TrainerModel> trainers;
}

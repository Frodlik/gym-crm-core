package com.gym.crm.dto.trainee;

import com.gym.crm.dto.model.TrainerModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TraineeGetResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private boolean isActive;
    private LocalDate dateOfBirth;
    private String address;
    private Set<TrainerModel> trainers = new HashSet<>();
}

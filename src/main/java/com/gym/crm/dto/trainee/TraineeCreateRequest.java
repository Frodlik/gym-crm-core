package com.gym.crm.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraineeCreateRequest {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
}

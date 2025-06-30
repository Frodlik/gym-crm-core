package com.gym.crm.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TraineeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private boolean isActive;
    private LocalDate dateOfBirth;
    private String address;
}

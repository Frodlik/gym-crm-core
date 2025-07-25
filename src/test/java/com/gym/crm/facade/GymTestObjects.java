package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.model.TrainerModel;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainingCriteriaRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.dto.trainer.AvailableTrainerResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateResponseDto;
import com.gym.crm.dto.trainer.TrainerGetResponseDto;
import com.gym.crm.dto.trainer.TrainerTrainingCriteriaRequest;
import com.gym.crm.dto.trainer.TrainerUpdateRequestDto;
import com.gym.crm.dto.trainer.TrainerUpdateResponseDto;
import com.gym.crm.dto.training.TrainingCreateRequestDto;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.model.TrainingType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GymTestObjects {
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";
    public static final String USERNAME = "john.doe";
    public static final String PASSWORD = "LuNdYs6oTA";
    public static final String TRAINER_FIRST_NAME = "Mike";
    public static final String TRAINER_LAST_NAME = "Johnson";
    public static final String TRAINER_USERNAME = "mike.johnson";
    public static final String TRAINING_NAME = "Morning Workout";
    public static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    public static final LocalDate TRAINING_DATE = LocalDate.of(2024, 1, 15);
    public static final String ADDRESS = "123 Main St";
    public static final String FITNESS_TYPE = "FITNESS";
    public static final String YOGA_TYPE = "YOGA";
    public static final int TRAINING_DURATION = 60;
    public static final Long TRAINEE_ID = 1L;
    public static final Long TRAINER_ID = 2L;
    public static final Long TRAINING_ID = 3L;

    public static TraineeCreateRequestDto buildTraineeCreateRequestDto() {
        return TraineeCreateRequestDto.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .dateOfBirth(BIRTH_DATE)
                .address(ADDRESS)
                .build();
    }

    public static TraineeCreateResponseDto buildTraineeCreateResponseDto() {
        return TraineeCreateResponseDto.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
    }

    public static TraineeUpdateRequestDto buildTraineeUpdateRequestDto() {
        return TraineeUpdateRequestDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .isActive(false)
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .address("456 Oak Ave")
                .build();
    }

    public static TraineeUpdateResponseDto buildTraineeUpdateResponseDto() {
        Set<TrainerModel> trainers = new HashSet<>();
        trainers.add(buildTrainerModel());

        return TraineeUpdateResponseDto.builder()
                .username(USERNAME)
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .address("456 Oak Ave")
                .isActive(false)
                .trainers(trainers)
                .build();
    }

    public static TraineeGetResponseDto buildTraineeGetResponseDto() {
        Set<TrainerModel> trainers = new HashSet<>();
        trainers.add(buildTrainerModel());

        return TraineeGetResponseDto.builder()
                .id(TRAINEE_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .isActive(true)
                .dateOfBirth(BIRTH_DATE)
                .address(ADDRESS)
                .trainers(trainers)
                .build();
    }

    public static TrainerCreateRequestDto buildTrainerCreateRequest() {
        return TrainerCreateRequestDto.builder()
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .specialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build())
                .build();
    }

    public static TrainerCreateResponseDto buildTrainerCreateResponseDto() {
        return TrainerCreateResponseDto.builder()
                .username(TRAINER_USERNAME)
                .password(PASSWORD)
                .build();
    }

    public static TrainerUpdateRequestDto buildTrainerUpdateRequest() {
        return TrainerUpdateRequestDto.builder()
                .firstName("Michael")
                .lastName("Smith")
                .isActive(false)
                .specialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build())
                .build();
    }

    public static TrainerUpdateResponseDto buildTrainerUpdateResponseDto() {
        return TrainerUpdateResponseDto.builder()
                .username(TRAINER_USERNAME)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .specialization(FITNESS_TYPE)
                .isActive(true)
                .build();
    }

    public static TrainerGetResponseDto buildTrainerResponse() {
        return TrainerGetResponseDto.builder()
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .isActive(true)
                .specialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build())
                .build();
    }

    public static TrainerModel buildTrainerModel() {
        return TrainerModel.builder()
                .username(TRAINER_USERNAME)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .specialization(TrainingType.builder().trainingTypeName(FITNESS_TYPE).build())
                .build();
    }

    public static AvailableTrainerResponseDto buildAvailableTrainerResponseDto() {
        return AvailableTrainerResponseDto.builder()
                .username(TRAINER_USERNAME)
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .specialization(TrainingType.builder()
                        .trainingTypeName(FITNESS_TYPE)
                        .build())
                .build();
    }

    public static TrainingCreateRequestDto buildTrainingCreateRequest() {
        return TrainingCreateRequestDto.builder()
                .traineeUsername(USERNAME)
                .trainerUsername(TRAINER_USERNAME)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }

    public static TrainingResponse buildTrainingResponse() {
        return TrainingResponse.builder()
                .id(TRAINING_ID)
                .traineeName(FIRST_NAME)
                .trainerName(TRAINER_FIRST_NAME)
                .trainingName(TRAINING_NAME)
                .trainingTypeName(FITNESS_TYPE)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }

    public static PasswordChangeRequest buildPasswordChangeRequest() {
        return new PasswordChangeRequest(
                USERNAME,
                "OldPassword123",
                "NewPassword456"
        );
    }

    public static TraineeTrainersUpdateRequestDto buildTraineeTrainersUpdateRequestDto() {
        return TraineeTrainersUpdateRequestDto.builder()
                .trainerUsernames(List.of(TRAINER_USERNAME, "trainer2.username"))
                .build();
    }

    public static TraineeTrainersUpdateResponseDto buildTraineeTrainersUpdateResponseDto() {
        List<TrainerModel> trainers = List.of(
                buildTrainerModel(),
                TrainerModel.builder()
                        .username("trainer2.username")
                        .firstName("Sarah")
                        .lastName("Wilson")
                        .specialization(TrainingType.builder().trainingTypeName(YOGA_TYPE).build())
                        .build()
        );

        return TraineeTrainersUpdateResponseDto.builder()
                .trainers(trainers)
                .build();
    }

    public static TraineeTrainingCriteriaRequestDto buildTraineeTrainingCriteriaRequestDto() {
        return TraineeTrainingCriteriaRequestDto.builder()
                .traineeUsername(USERNAME)
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .trainerName(TRAINER_FIRST_NAME)
                .trainingType(FITNESS_TYPE)
                .build();
    }

    public static TrainerTrainingCriteriaRequest buildTrainerTrainingCriteriaRequest() {
        return TrainerTrainingCriteriaRequest.builder()
                .trainerUsername(TRAINER_USERNAME)
                .fromDate(LocalDate.of(2024, 1, 1))
                .toDate(LocalDate.of(2024, 12, 31))
                .traineeName(FIRST_NAME)
                .build();
    }
}
package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.model.Training;
import com.gym.crm.repository.specification.TrainingSpecifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingSpecificationsTest extends BaseIntegrationTest {
    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void forTraineeSearch_WhenValidFilter_ThenReturnsFilteredTrainings() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .fromDate(LocalDate.of(2024, 8, 1))
                .toDate(LocalDate.of(2024, 8, 31))
                .trainerName("Alex")
                .trainingType("Strength")
                .build();

        Specification<Training> spec = TrainingSpecifications.forTraineeSearch(filter);
        List<Training> actual = trainingRepository.findAll(spec);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals("john.trainee", actual.getFirst().getTrainee().getUser().getUsername());
        assertTrue(actual.getFirst().getTrainer().getUser().getFirstName().contains("Alex"));
        assertTrue(actual.getFirst().getTrainingType().getTrainingTypeName().contains("Strength"));
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void forTrainerSearch_WhenValidFilter_ThenReturnsFilteredTrainings() {
        TrainerSearchFilter filter = TrainerSearchFilter.builder()
                .trainerUsername("alex.trainer")
                .fromDate(LocalDate.of(2024, 8, 1))
                .toDate(LocalDate.of(2024, 8, 31))
                .traineeName("John")
                .build();

        Specification<Training> spec = TrainingSpecifications.forTrainerSearch(filter);
        List<Training> actual = trainingRepository.findAll(spec);

        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertEquals("alex.trainer", actual.getFirst().getTrainer().getUser().getUsername());
        assertTrue(actual.getFirst().getTrainee().getUser().getFirstName().contains("John"));
    }

    @ParameterizedTest
    @MethodSource("provideTraineeSearchScenarios")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void forTraineeSearch_WhenDifferentFilters_ThenReturnsExpectedResults(TraineeSearchScenario scenario) {
        TraineeSearchFilter filter = scenario.filter();

        Specification<Training> spec = TrainingSpecifications.forTraineeSearch(filter);
        List<Training> actual = trainingRepository.findAll(spec);

        assertEquals(scenario.expectedCount(), actual.size(), scenario.description());
    }

    @ParameterizedTest
    @MethodSource("provideTrainerSearchScenarios")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void forTrainerSearch_WhenDifferentFilters_ThenReturnsExpectedResults(TrainerSearchScenario scenario) {
        TrainerSearchFilter filter = scenario.filter();

        Specification<Training> spec = TrainingSpecifications.forTrainerSearch(filter);
        List<Training> actual = trainingRepository.findAll(spec);

        assertEquals(scenario.expectedCount(), actual.size(), scenario.description());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void forTraineeSearch_WhenEmptyFilter_ThenReturnsAllTrainings() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .build();

        Specification<Training> spec = TrainingSpecifications.forTraineeSearch(filter);
        List<Training> actual = trainingRepository.findAll(spec);

        assertEquals(2, actual.size());
        actual.forEach(training ->
                assertEquals("john.trainee", training.getTrainee().getUser().getUsername())
        );
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void forTraineeSearch_WhenNoMatches_ThenReturnsEmptyList() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername("nonexistent.user")
                .build();

        Specification<Training> spec = TrainingSpecifications.forTraineeSearch(filter);
        List<Training> actual = trainingRepository.findAll(spec);

        assertTrue(actual.isEmpty());
    }

    private static Stream<Arguments> provideTraineeSearchScenarios() {
        return Stream.of(
                Arguments.of(new TraineeSearchScenario(
                        "All trainings for specific trainee",
                        TraineeSearchFilter.builder()
                                .traineeUsername("john.trainee")
                                .build(),
                        2
                )),
                Arguments.of(new TraineeSearchScenario(
                        "Trainings with date range",
                        TraineeSearchFilter.builder()
                                .traineeUsername("john.trainee")
                                .fromDate(LocalDate.of(2024, 8, 10))
                                .toDate(LocalDate.of(2024, 8, 15))
                                .build(),
                        2
                )),
                Arguments.of(new TraineeSearchScenario(
                        "Trainings with specific trainer",
                        TraineeSearchFilter.builder()
                                .traineeUsername("john.trainee")
                                .trainerName("Alex")
                                .build(),
                        2
                )),
                Arguments.of(new TraineeSearchScenario(
                        "Trainings with specific type",
                        TraineeSearchFilter.builder()
                                .traineeUsername("john.trainee")
                                .trainingType("Cardio")
                                .build(),
                        0
                ))
        );
    }

    private static Stream<Arguments> provideTrainerSearchScenarios() {
        return Stream.of(
                Arguments.of(new TrainerSearchScenario(
                        "All trainings for specific trainer",
                        TrainerSearchFilter.builder()
                                .trainerUsername("alex.trainer")
                                .build(),
                        2
                )),
                Arguments.of(new TrainerSearchScenario(
                        "Trainings with specific trainee",
                        TrainerSearchFilter.builder()
                                .trainerUsername("alex.trainer")
                                .traineeName("John")
                                .build(),
                        2
                )),
                Arguments.of(new TrainerSearchScenario(
                        "Trainings in future date range",
                        TrainerSearchFilter.builder()
                                .trainerUsername("alex.trainer")
                                .fromDate(LocalDate.of(2025, 1, 1))
                                .toDate(LocalDate.of(2025, 12, 31))
                                .build(),
                        0
                ))
        );
    }

    private record TraineeSearchScenario(
            String description,
            TraineeSearchFilter filter,
            int expectedCount
    ) {

    }

    private record TrainerSearchScenario(
            String description,
            TrainerSearchFilter filter,
            int expectedCount
    ) {

    }
}

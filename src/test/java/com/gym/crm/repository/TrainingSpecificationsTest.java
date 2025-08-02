package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
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
    void hasTraineeUsername_ShouldFindTrainingsForSpecificTrainee() {
        String traineeUsername = "john.trainee";
        Specification<Training> spec = TrainingSpecifications.hasTraineeUsername(traineeUsername);

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(2, trainings.size());
        trainings.forEach(training ->
                assertEquals(traineeUsername, training.getTrainee().getUser().getUsername())
        );
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void hasTrainerUsername_ShouldFindTrainingsForSpecificTrainer() {
        String trainerUsername = "alex.trainer";
        Specification<Training> spec = TrainingSpecifications.hasTrainerUsername(trainerUsername);

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(2, trainings.size());
        trainings.forEach(training ->
                assertEquals(trainerUsername, training.getTrainer().getUser().getUsername())
        );
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void hasTrainingDateBetween_ShouldFindTrainingsInDateRange() {
        LocalDate fromDate = LocalDate.of(2024, 8, 10);
        LocalDate toDate = LocalDate.of(2024, 8, 15);
        Specification<Training> spec = TrainingSpecifications.hasTrainingDateBetween(fromDate, toDate);

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(4, trainings.size());
        trainings.forEach(training -> {
            assertTrue(training.getTrainingDate().isAfter(fromDate.minusDays(1)));
            assertTrue(training.getTrainingDate().isBefore(toDate.plusDays(1)));
        });
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void hasTrainerNameContaining_ShouldFindTrainingsByTrainerName() {
        String trainerName = "Alex";
        Specification<Training> spec = TrainingSpecifications.hasTrainerNameContaining(trainerName);

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(2, trainings.size());
        trainings.forEach(training -> {
            String fullName = training.getTrainer().getUser().getFirstName() + " " +
                    training.getTrainer().getUser().getLastName();
            assertTrue(fullName.toLowerCase().contains(trainerName.toLowerCase()));
        });
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void hasTraineeNameContaining_ShouldFindTrainingsByTraineeName() {
        String traineeName = "John";
        Specification<Training> spec = TrainingSpecifications.hasTraineeNameContaining(traineeName);

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(2, trainings.size());
        trainings.forEach(training -> {
            String fullName = training.getTrainee().getUser().getFirstName() + " " +
                    training.getTrainee().getUser().getLastName();
            assertTrue(fullName.toLowerCase().contains(traineeName.toLowerCase()));
        });
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void hasTrainingTypeContaining_ShouldFindTrainingsByType() {
        String trainingType = "Strength";
        Specification<Training> spec = TrainingSpecifications.hasTrainingTypeContaining(trainingType);

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(1, trainings.size());
        trainings.forEach(training ->
                assertTrue(training.getTrainingType().getTrainingTypeName().toLowerCase()
                        .contains(trainingType.toLowerCase()))
        );
    }

    @ParameterizedTest
    @MethodSource("provideComplexSearchCriteria")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void complexSearch_ShouldCombineMultipleSpecifications(SearchCriteria criteria) {
        Specification<Training> spec = Specification
                .where(TrainingSpecifications.hasTraineeUsername(criteria.traineeUsername()))
                .and(TrainingSpecifications.hasTrainingDateBetween(criteria.fromDate(), criteria.toDate()))
                .and(TrainingSpecifications.hasTrainerNameContaining(criteria.trainerName()))
                .and(TrainingSpecifications.hasTrainingTypeContaining(criteria.trainingType()))
                .and(TrainingSpecifications.withEagerFetching())
                .and(TrainingSpecifications.orderByTrainingDateDesc());

        List<Training> trainings = trainingRepository.findAll(spec);

        assertNotNull(trainings);
        assertEquals(criteria.expectedCount(), trainings.size());
    }

    private static Stream<Arguments> provideComplexSearchCriteria() {
        return Stream.of(
                Arguments.of(new SearchCriteria(
                        "john.trainee",
                        LocalDate.of(2024, 8, 1),
                        LocalDate.of(2024, 8, 31),
                        "Alex",
                        "Strength",
                        1
                )),
                Arguments.of(new SearchCriteria(
                        "john.trainee",
                        null,
                        null,
                        null,
                        null,
                        2
                )),
                Arguments.of(new SearchCriteria(
                        "john.trainee",
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 1, 31),
                        null,
                        null,
                        0
                ))
        );
    }

    private record SearchCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType,
            int expectedCount
    ) {
    }
}

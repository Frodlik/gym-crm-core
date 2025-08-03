package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.repository.specification.TrainingSpecifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private TrainingRepository trainingRepository;
    @Autowired
    private TraineeRepository traineeRepository;
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithYogaSpecializationAndCorrectDuration() {
        Training trainingToCreate = buildTrainingFromDataset(1L, 2L, "Strength", 2L, 60);

        Training actual = trainingRepository.save(trainingToCreate);

        assertNotNull(actual);
        assertNotNull(actual.getId());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithFlexibilitySpecializationAndExtendedDuration() {
        Training trainingToCreate = buildTrainingFromDataset(2L, 1L, "Power Lifting Session", 1L, 90);

        Training actual = trainingRepository.save(trainingToCreate);

        Training savedTraining = trainingRepository.findById(actual.getId()).orElseThrow();

        assertNotNull(actual);
        assertEquals("Power Lifting Session", savedTraining.getTrainingName());
        assertEquals(90, savedTraining.getTrainingDuration());
        assertEquals("Strength", savedTraining.getTrainingType().getTrainingTypeName());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithMinimalDataAndDefaultCardioType() {
        LocalDate todayDate = LocalDate.now();
        Training trainingToCreate = buildTrainingFromDataset(1L, 3L, "Quick Cardio Session", 3L, 30);
        trainingToCreate = trainingToCreate.toBuilder()
                .trainingDate(todayDate)
                .build();

        Training actual = trainingRepository.save(trainingToCreate);

        Training savedTraining = trainingRepository.findById(actual.getId()).orElseThrow();

        assertNotNull(actual);
        assertEquals("Quick Cardio Session", savedTraining.getTrainingName());
        assertEquals(todayDate, savedTraining.getTrainingDate());
        assertEquals(30, savedTraining.getTrainingDuration());
        assertEquals("Cardio", savedTraining.getTrainingType().getTrainingTypeName());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnTrainingWhenExists() {
        Long existingTrainingId = 1L;

        Optional<Training> actual = trainingRepository.findById(existingTrainingId);

        assertTrue(actual.isPresent());
        assertEquals(existingTrainingId, actual.get().getId());
        assertEquals("Power Strength Training", actual.get().getTrainingName());
        assertEquals(LocalDate.of(2024, 8, 10), actual.get().getTrainingDate());
        assertEquals(75, actual.get().getTrainingDuration());
        assertEquals("Strength", actual.get().getTrainingType().getTrainingTypeName());
        assertEquals("alex.trainer", actual.get().getTrainer().getUser().getUsername());
        assertEquals("john.trainee", actual.get().getTrainee().getUser().getUsername());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnEmptyWhenTrainingNotExists() {
        Long nonExistentTrainingId = 999L;

        Optional<Training> actual = trainingRepository.findById(nonExistentTrainingId);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnAllExistingTrainings() {
        int expectedTrainingsCount = 4;

        List<Training> actualTrainingsList = trainingRepository.findAll();

        Training firstTraining = actualTrainingsList.stream()
                .filter(t -> t.getTrainingName().equals("Power Strength Training"))
                .findFirst().orElseThrow();
        Training secondTraining = actualTrainingsList.stream()
                .filter(t -> t.getTrainingName().equals("Relaxing Yoga Session"))
                .findFirst().orElseThrow();

        assertNotNull(actualTrainingsList);
        assertEquals(expectedTrainingsCount, actualTrainingsList.size());
        assertEquals(75, firstTraining.getTrainingDuration());
        assertEquals(90, secondTraining.getTrainingDuration());
        assertEquals("alex.trainer", firstTraining.getTrainer().getUser().getUsername());
        assertEquals("maria.trainer", secondTraining.getTrainer().getUser().getUsername());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testDelete_ShouldRemoveTrainingFromDatabase() {
        Long trainingId = 1L;
        assertTrue(trainingRepository.findById(trainingId).isPresent());

        trainingRepository.deleteById(trainingId);

        assertFalse(trainingRepository.findById(trainingId).isPresent());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testExistsById_ShouldReturnTrueWhenTrainingExists() {
        Long existingTrainingId = 1L;

        boolean exists = trainingRepository.existsById(existingTrainingId);

        assertTrue(exists);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testExistsById_ShouldReturnFalseWhenTrainingNotExists() {
        Long nonExistentTrainingId = 999L;

        boolean exists = trainingRepository.existsById(nonExistentTrainingId);

        assertFalse(exists);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCount_ShouldReturnCorrectNumberOfTrainings() {
        long expectedCount = 4L;

        long actualCount = trainingRepository.count();

        assertEquals(expectedCount, actualCount);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testForTrainerSearch_WhenValidFilter_ThenReturnsFilteredTrainings() {
        TrainerSearchFilter filter = TrainerSearchFilter.builder()
                .trainerUsername("alex.trainer")
                .fromDate(LocalDate.of(2024, 8, 1))
                .toDate(LocalDate.of(2024, 8, 31))
                .traineeName("John")
                .build();
        Specification<Training> specification = TrainingSpecifications.forTrainerSearch(filter);

        List<Training> actual = trainingRepository.findAll(specification);

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting(training -> training.getTrainer().getUser().getUsername())
                .containsOnly("alex.trainer");
        assertThat(actual)
                .extracting(training -> training.getTrainee().getUser().getFirstName())
                .allMatch(name -> name.contains("John"));
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testForTraineeSearch_WhenEmptyFilter_ThenReturnsAllTrainingsForTrainee() {
        TraineeSearchFilter filter = TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .build();
        Specification<Training> specification = TrainingSpecifications.forTraineeSearch(filter);

        List<Training> actual = trainingRepository.findAll(specification);

        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting(training -> training.getTrainee().getUser().getUsername())
                .containsOnly("john.trainee");
    }

    @ParameterizedTest
    @MethodSource("provideTraineeSearchScenarios")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testForTraineeSearch_WhenDifferentFilters_ThenReturnsExpectedResults(String description, TraineeSearchFilter filter, int expectedCount) {
        Specification<Training> specification = TrainingSpecifications.forTraineeSearch(filter);

        List<Training> actual = trainingRepository.findAll(specification);

        assertThat(actual)
                .as(description)
                .hasSize(expectedCount);
    }

    @ParameterizedTest
    @MethodSource("provideTrainerSearchScenarios")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testForTrainerSearch_WhenDifferentFilters_ThenReturnsExpectedResults(String description, TrainerSearchFilter filter, int expectedCount) {
        Specification<Training> specification = TrainingSpecifications.forTrainerSearch(filter);

        List<Training> actual = trainingRepository.findAll(specification);

        assertThat(actual)
                .as(description)
                .hasSize(expectedCount);
    }

    private static Stream<Arguments> provideTraineeSearchScenarios() {
        return Stream.of(
                Arguments.of("All trainings for specific trainee", traineeFilter(), 2),
                Arguments.of("Trainings with date range", traineeFilterWithDates(LocalDate.of(2024, 8, 10), LocalDate.of(2024, 8, 15)), 2),
                Arguments.of("Trainings with specific trainer", traineeFilterWithTrainer(), 2),
                Arguments.of("Trainings with specific type", traineeFilterWithType(), 0)
        );
    }

    private static Stream<Arguments> provideTrainerSearchScenarios() {
        return Stream.of(
                Arguments.of("All trainings for specific trainer", trainerFilter(), 2),
                Arguments.of("Trainings with specific trainee", trainerFilterWithTrainee(), 2),
                Arguments.of("Trainings in future date range", trainerFilterWithDates(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)), 0)
        );
    }

    private static TraineeSearchFilter traineeFilter() {
        return TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .build();
    }

    private static TraineeSearchFilter traineeFilterWithDates(LocalDate from, LocalDate to) {
        return TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .fromDate(from)
                .toDate(to)
                .build();
    }

    private static TraineeSearchFilter traineeFilterWithTrainer() {
        return TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .trainerName("Alex")
                .build();
    }

    private static TraineeSearchFilter traineeFilterWithType() {
        return TraineeSearchFilter.builder()
                .traineeUsername("john.trainee")
                .trainingType("Cardio")
                .build();
    }

    private static TrainerSearchFilter trainerFilter() {
        return TrainerSearchFilter.builder()
                .trainerUsername("alex.trainer")
                .build();
    }

    private static TrainerSearchFilter trainerFilterWithTrainee() {
        return TrainerSearchFilter.builder()
                .trainerUsername("alex.trainer")
                .traineeName("John")
                .build();
    }

    private static TrainerSearchFilter trainerFilterWithDates(LocalDate from, LocalDate to) {
        return TrainerSearchFilter.builder()
                .trainerUsername("alex.trainer")
                .fromDate(from)
                .toDate(to)
                .build();
    }

    private Training buildTrainingFromDataset(Long traineeId, Long trainerId, String trainingName, Long trainingTypeId, int duration) {
        Trainee trainee = traineeRepository.findById(traineeId).orElseThrow();
        Trainer trainer = trainerRepository.findById(trainerId).orElseThrow();
        TrainingType trainingType = trainingTypeRepository.findById(trainingTypeId).orElseThrow();

        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(LocalDate.of(2024, 8, 15))
                .trainingDuration(duration)
                .build();
    }
}

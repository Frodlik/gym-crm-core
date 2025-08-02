package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("provideTraineeCriteriaTestCases")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindTraineeTrainingsByCriteria_Parameterized(CriteriaTestCase testCase) {
        List<Training> actual = trainingRepository.findTraineeTrainingsByCriteria(
                testCase.traineeUsername(), testCase.fromDate(), testCase.toDate(),
                testCase.trainerName(), testCase.trainingType()
        );

        assertNotNull(actual);
        assertEquals(testCase.expectedSize(), actual.size());
    }

    @ParameterizedTest
    @MethodSource("provideTrainerCriteriaTestCases")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindTrainerTrainingsByCriteria_Parameterized(CriteriaTrainerTestCase testCase) {
        List<Training> actual = trainingRepository.findTrainerTrainingsByCriteria(
                testCase.trainerUsername(), testCase.fromDate(), testCase.toDate(), testCase.traineeName()
        );

        assertNotNull(actual);
        assertEquals(testCase.expectedSize(), actual.size());
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

    private static Stream<Arguments> provideTraineeCriteriaTestCases() {
        return Stream.of(
                Arguments.of(new CriteriaTestCase(
                        "john.trainee", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 31),
                        "Alex", "Strength", 1, "Power Strength Training"
                )),
                Arguments.of(new CriteriaTestCase(
                        "john.trainee", null, null, null, null, 2, null
                )),
                Arguments.of(new CriteriaTestCase(
                        "john.trainee", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                        null, null, 0, null
                )),
                Arguments.of(new CriteriaTestCase(
                        "john.trainee", null, null, null, "HIIT", 1, "Full Body HIIT Session"
                )),
                Arguments.of(new CriteriaTestCase(
                        "john.trainee", LocalDate.of(2024, 8, 12), LocalDate.of(2024, 8, 15),
                        null, null, 1, "Full Body HIIT Session"
                ))
        );
    }

    private static Stream<Arguments> provideTrainerCriteriaTestCases() {
        return Stream.of(
                Arguments.of(new CriteriaTrainerTestCase(
                        "alex.trainer", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 31),
                        "John", 2, "Full Body HIIT Session"
                )),
                Arguments.of(new CriteriaTrainerTestCase(
                        "maria.trainer", null, null, null, 1, "Relaxing Yoga Session"
                )),
                Arguments.of(new CriteriaTrainerTestCase(
                        "chris.coach", LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 11),
                        null, 0, null
                )),
                Arguments.of(new CriteriaTrainerTestCase(
                        "alex.trainer", null, null, "John", 2, "Full Body HIIT Session"
                )),
                Arguments.of(new CriteriaTrainerTestCase(
                        "alex.trainer", LocalDate.of(2024, 8, 10), LocalDate.of(2024, 8, 10),
                        null, 1, "Power Strength Training"
                ))
        );
    }

    private record CriteriaTestCase(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType,
            int expectedSize,
            String expectedTrainingName
    ) {
    }

    private record CriteriaTrainerTestCase(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName,
            int expectedSize,
            String expectedTrainingName
    ) {
    }
}

package com.gym.crm.dao.impl;

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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingDAOImplTest extends BaseIntegrationTest<TrainingDAOImpl> {
    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithYogaSpecializationAndCorrectDuration() {
        Training trainingToCreate = buildTrainingFromDataset(1L, 2L, "Morning Yoga Session", 2L, 60);

        Training actual = dao.create(trainingToCreate);

        String actualTrainingTypeName = getTrainingTypeNameById(actual.getId());

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals("Morning Yoga Session", actual.getTrainingName());
        assertEquals(60, actual.getTrainingDuration());
        assertEquals(LocalDate.of(2024, 8, 15), actual.getTrainingDate());
        assertEquals("Yoga", actualTrainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithFlexibilitySpecializationAndExtendedDuration() {
        Training trainingToCreate = buildTrainingFromDataset(2L, 1L, "Power Lifting Session", 1L, 90);

        Training actual = dao.create(trainingToCreate);

        String actualTrainingTypeName = getTrainingTypeNameById(actual.getId());

        assertNotNull(actual);
        assertEquals("Power Lifting Session", actual.getTrainingName());
        assertEquals(90, actual.getTrainingDuration());
        assertEquals("Strength", actualTrainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainingWithMinimalDataAndDefaultCardioType() {
        LocalDate todayDate = LocalDate.now();
        Training trainingToCreate = buildTrainingFromDataset(1L, 3L, "Quick Cardio Session", 3L, 30);
        trainingToCreate = trainingToCreate.toBuilder()
                .trainingDate(todayDate)
                .build();

        Training actual = dao.create(trainingToCreate);

        String actualTrainingTypeName = getTrainingTypeNameById(actual.getId());

        assertNotNull(actual);
        assertEquals("Quick Cardio Session", actual.getTrainingName());
        assertEquals(todayDate, actual.getTrainingDate());
        assertEquals(30, actual.getTrainingDuration());
        assertEquals("Cardio", actualTrainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnTrainingWhenExists() {
        Long existingTrainingId = 1L;

        Training actual = dao.findById(existingTrainingId).orElseThrow();

        doInSession(session -> {
            Training persistedTraining = session.get(Training.class, existingTrainingId);
            assertEquals(existingTrainingId, actual.getId());
            assertEquals("Power Strength Training", actual.getTrainingName());
            assertEquals(LocalDate.of(2024, 8, 10), actual.getTrainingDate());
            assertEquals(75, actual.getTrainingDuration());
            assertEquals("Strength", persistedTraining.getTrainingType().getTrainingTypeName());
            assertEquals("alex.trainer", persistedTraining.getTrainer().getUser().getUsername());
            assertEquals("john.trainee", persistedTraining.getTrainee().getUser().getUsername());
        });
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnEmptyWhenTrainingNotExists() {
        Long nonExistentTrainingId = 999L;

        Optional<Training> actual = dao.findById(nonExistentTrainingId);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnAllExistingTrainings() {
        int expectedTrainingsCount = 4;

        List<Training> actualTrainingsList = dao.findAll();

        doInSession(session -> {
            Training firstTraining = actualTrainingsList.get(0);
            Training secondTraining = actualTrainingsList.get(1);

            Training freshFirstTraining = session.get(Training.class, firstTraining.getId());
            Training freshSecondTraining = session.get(Training.class, secondTraining.getId());

            assertNotNull(actualTrainingsList);
            assertEquals(expectedTrainingsCount, actualTrainingsList.size());
            assertEquals("Power Strength Training", firstTraining.getTrainingName());
            assertEquals(75, firstTraining.getTrainingDuration());
            assertEquals("Relaxing Yoga Session", secondTraining.getTrainingName());
            assertEquals(90, secondTraining.getTrainingDuration());
            assertEquals("alex.trainer", freshFirstTraining.getTrainer().getUser().getUsername());
            assertEquals("maria.trainer", freshSecondTraining.getTrainer().getUser().getUsername());
        });
    }

    @ParameterizedTest
    @MethodSource("provideTraineeCriteriaTestCases")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindTraineeTrainingsByCriteria_Parameterized(CriteriaTestCase testCase) {
        List<Training> actual = dao.findTraineeTrainingsByCriteria(
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
        List<Training> actual = dao.findTrainerTrainingsByCriteria(
                testCase.trainerUsername(), testCase.fromDate(), testCase.toDate(), testCase.traineeName()
        );

        assertNotNull(actual);
        assertEquals(testCase.expectedSize(), actual.size());
    }

    private Training buildTrainingFromDataset(Long traineeId, Long trainerId, String trainingName, Long trainingTypeId, int duration) {
        return doInSession(session -> {
            Trainee trainee = session.get(Trainee.class, traineeId);
            Trainer trainer = session.get(Trainer.class, trainerId);
            TrainingType trainingType = session.get(TrainingType.class, trainingTypeId);

            return Training.builder()
                    .trainee(trainee)
                    .trainer(trainer)
                    .trainingName(trainingName)
                    .trainingType(trainingType)
                    .trainingDate(LocalDate.of(2024, 8, 15))
                    .trainingDuration(duration)
                    .build();
        });
    }

    private String getTrainingTypeNameById(Long trainingId) {
        return doInSession(session -> {
            Training persistedTraining = session.get(Training.class, trainingId);

            return persistedTraining.getTrainingType().getTrainingTypeName();
        });
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

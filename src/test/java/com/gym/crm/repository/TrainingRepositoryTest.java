package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void delete_ShouldRemoveTrainingFromDatabase() {
        Long trainingId = 1L;
        assertTrue(trainingRepository.findById(trainingId).isPresent());

        trainingRepository.deleteById(trainingId);

        assertFalse(trainingRepository.findById(trainingId).isPresent());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void existsById_ShouldReturnTrueWhenTrainingExists() {
        Long existingTrainingId = 1L;

        boolean exists = trainingRepository.existsById(existingTrainingId);

        assertTrue(exists);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void existsById_ShouldReturnFalseWhenTrainingNotExists() {
        Long nonExistentTrainingId = 999L;

        boolean exists = trainingRepository.existsById(nonExistentTrainingId);

        assertFalse(exists);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void count_ShouldReturnCorrectNumberOfTrainings() {
        long expectedCount = 4L;

        long actualCount = trainingRepository.count();

        assertEquals(expectedCount, actualCount);
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

package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrainerRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findTrainerByUserUsername_whenTrainerExists_shouldReturnTrainer() {
        String existingUsername = "sarah.johnson";

        Optional<Trainer> actual = trainerRepository.findTrainerByUser_Username(existingUsername);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getUsername()).isEqualTo(existingUsername);
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Sarah");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Johnson");
        assertThat(actual.get().getUser().getIsActive()).isTrue();
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findTrainerByUserUsername_whenTrainerNotExists_shouldReturnEmpty() {
        String nonExistentUsername = "kakashi.hatake";

        Optional<Trainer> actual = trainerRepository.findTrainerByUser_Username(nonExistentUsername);

        assertThat(actual).isEmpty();
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testCreate_ShouldPersistTrainerWithHIITSpecializationAndInactiveStatus() {
        Trainer trainerToCreate = createSampleTrainerWithSpecialization();

        Trainer actualTrainer = trainerRepository.save(trainerToCreate);

        assertNotNull(actualTrainer);
        assertNotNull(actualTrainer.getId());
        assertEquals("HIIT", actualTrainer.getSpecialization().getTrainingTypeName());
        assertFalse(actualTrainer.getUser().getIsActive());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findTrainersNotAssignedToTrainee_whenTraineeNotExists_shouldReturnActiveTrainers() {
        String nonExistentTraineeUsername = "hinata.hyuga";

        List<Trainer> actual = trainerRepository.findTrainersNotAssignedToTrainee(nonExistentTraineeUsername);

        assertThat(actual).hasSize(2);
        assertThat(actual).allMatch(trainer -> trainer.getUser().getIsActive());
        assertThat(actual).extracting(trainer -> trainer.getUser().getUsername())
                .containsExactlyInAnyOrder("sarah.johnson", "anna.davis");
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindById_ShouldReturnEmptyWhenTrainerNotExists() {
        Long nonExistentTrainerId = 999L;

        Optional<Trainer> actual = trainerRepository.findById(nonExistentTrainerId);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findAll_shouldReturnAllTrainers() {
        List<Trainer> actual = trainerRepository.findAll();

        assertThat(actual).hasSize(3);
        assertThat(actual).extracting(trainer -> trainer.getUser().getUsername())
                .containsExactlyInAnyOrder("sarah.johnson", "mike.wilson", "anna.davis");
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findById_whenTrainerExists_shouldReturnTrainer() {
        Long existingId = 1L;

        Optional<Trainer> actual = trainerRepository.findById(existingId);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Sarah");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Johnson");
    }

    @Test
    @DataSet(value = "dataset/trainer-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findById_whenTrainerNotExists_shouldReturnEmpty() {
        Long nonExistentId = 999L;

        Optional<Trainer> actual = trainerRepository.findById(nonExistentId);

        assertThat(actual).isEmpty();
    }

    private Trainer createSampleTrainerWithSpecialization() {
        TrainingType specialization = trainingTypeRepository.findByTrainingTypeName("HIIT").get();

        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("password456")
                .isActive(false)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();
    }
}

package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findTraineeByUserUsername_whenTraineeExists_shouldReturnTrainee() {
        String existingUsername = "emma.miller";

        Optional<Trainee> result = traineeRepository.findTraineeByUser_Username(existingUsername);

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo(existingUsername);
        assertThat(result.get().getUser().getFirstName()).isEqualTo("Emma");
        assertThat(result.get().getUser().getLastName()).isEqualTo("Miller");
        assertThat(result.get().getAddress()).isEqualTo("123 Main St, New York, NY 10001");
        assertThat(result.get().getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findTraineeByUserUsername_whenTraineeNotExists_shouldReturnEmpty() {
        String nonExistentUsername = "naruto.uzumaki";

        Optional<Trainee> actual = traineeRepository.findTraineeByUser_Username(nonExistentUsername);

        assertThat(actual).isEmpty();
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void deleteByUsername_whenTraineeExists_shouldDeleteTrainee() {
        String existingUsername = "emma.miller";

        traineeRepository.deleteByUser_Username(existingUsername);

        Optional<Trainee> result = traineeRepository.findTraineeByUser_Username(existingUsername);
        assertThat(result).isEmpty();
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void deleteByUsername_whenTraineeNotExists_shouldNotAffectOtherData() {
        String nonExistentUsername = "sasuke.uchiha";
        long initialCount = traineeRepository.count();

        traineeRepository.deleteByUser_Username(nonExistentUsername);

        long finalCount = traineeRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    @Test
    void save_whenValidTrainee_shouldPersistTrainee() {
        Trainee trainee = createTrainee();

        Trainee actual = traineeRepository.save(trainee);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getUsername()).isEqualTo("sakura.haruno");
        assertThat(actual.getUser().getFirstName()).isEqualTo("Sakura");
        assertThat(actual.getUser().getLastName()).isEqualTo("Haruno");
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findById_whenTraineeExists_shouldReturnTrainee() {
        Long existingId = 1L;

        Optional<Trainee> actual = traineeRepository.findById(existingId);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Emma");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Miller");
        assertThat(actual.get().getAddress()).isEqualTo("123 Main St, New York, NY 10001");
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findById_whenTraineeNotExists_shouldReturnEmpty() {
        Long nonExistentId = 999L;

        Optional<Trainee> actual = traineeRepository.findById(nonExistentId);

        assertThat(actual).isEmpty();
    }

    @Test
    @DataSet(value = "dataset/trainee-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findAll_shouldReturnAllTrainees() {
        var result = traineeRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getUsername()).isEqualTo("emma.miller");
    }

    private Trainee createTrainee() {
        User user = User.builder()
                .firstName("Sakura")
                .lastName("Haruno")
                .username("sakura.haruno")
                .password("password123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .address("Konoha Village")
                .build();
    }
}

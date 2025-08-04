package com.gym.crm.repository;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TrainingTypeRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @ParameterizedTest
    @MethodSource("provideValidTrainingTypeNames")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByTrainingTypeName_whenTrainingTypeExists_shouldReturnTrainingType(String trainingTypeName, Long expectedId) {
        Optional<TrainingType> actual = trainingTypeRepository.findByTrainingTypeName(trainingTypeName);

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(expectedId);
        assertThat(actual.get().getTrainingTypeName()).isEqualTo(trainingTypeName);
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByTrainingTypeName_whenTrainingTypeNotExists_shouldReturnEmpty() {
        String nonExistentTypeName = "Rasengan Training";

        Optional<TrainingType> actual = trainingTypeRepository.findByTrainingTypeName(nonExistentTypeName);

        assertThat(actual).isEmpty();
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findAll_shouldReturnAllTrainingTypes() {
        List<TrainingType> actual = trainingTypeRepository.findAll();

        assertThat(actual).hasSize(7);
        assertThat(actual).extracting(TrainingType::getTrainingTypeName)
                .containsExactlyInAnyOrder(
                        "Strength", "Yoga", "Cardio", "HIIT",
                        "Pilates", "Flexibility", "Boxing"
                );
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void save_whenValidTrainingType_shouldPersistTrainingType() {
        TrainingType trainingType = TrainingType.builder()
                .trainingTypeName("Sharingan Training")
                .build();

        TrainingType savedTrainingType = trainingTypeRepository.save(trainingType);

        assertThat(savedTrainingType.getId()).isNotNull();
        assertThat(savedTrainingType.getTrainingTypeName()).isEqualTo("Sharingan Training");
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findById_whenTrainingTypeExists_shouldReturnTrainingType() {
        Long existingId = 1L;

        Optional<TrainingType> actual = trainingTypeRepository.findById(existingId);

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainingTypeName()).isEqualTo("Strength");
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findById_whenTrainingTypeNotExists_shouldReturnEmpty() {
        Long nonExistentId = 999L;

        Optional<TrainingType> actual = trainingTypeRepository.findById(nonExistentId);

        assertThat(actual).isEmpty();
    }

    private static Stream<Arguments> provideValidTrainingTypeNames() {
        return Stream.of(
                Arguments.of("Strength", 1L),
                Arguments.of("Yoga", 2L),
                Arguments.of("Cardio", 3L),
                Arguments.of("HIIT", 4L),
                Arguments.of("Pilates", 5L),
                Arguments.of("Flexibility", 6L),
                Arguments.of("Boxing", 7L)
        );
    }
}

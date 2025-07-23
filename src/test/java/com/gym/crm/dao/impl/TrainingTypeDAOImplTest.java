package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingTypeDAOImplTest extends BaseIntegrationTest<TrainingTypeDAOImpl> {

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testGetByName_ShouldReturnTrainingTypeWhenExists() {
        String existingTrainingTypeName = "Strength";

        Optional<TrainingType> actual = dao.getByName(existingTrainingTypeName);

        assertTrue(actual.isPresent());
        assertNotNull(actual.get());
        assertEquals(existingTrainingTypeName, actual.get().getTrainingTypeName());
        assertNotNull(actual.get().getId());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testGetByName_ShouldReturnTrainingTypeForYoga() {
        String yogaTypeName = "Yoga";

        Optional<TrainingType> actual = dao.getByName(yogaTypeName);

        assertTrue(actual.isPresent());
        assertEquals(yogaTypeName, actual.get().getTrainingTypeName());
        assertEquals(Long.valueOf(2), actual.get().getId());
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testGetByName_ShouldReturnTrainingTypeForCardio() {
        String cardioTypeName = "Cardio";

        Optional<TrainingType> actual = dao.getByName(cardioTypeName);

        assertTrue(actual.isPresent());
        assertEquals(cardioTypeName, actual.get().getTrainingTypeName());
        assertEquals(Long.valueOf(3), actual.get().getId());
    }

    @ParameterizedTest
    @MethodSource("provideValidTrainingTypeNames")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testGetByName_ShouldReturnCorrectTrainingTypeForValidNames(String trainingTypeName, Long expectedId) {
        Optional<TrainingType> actual = dao.getByName(trainingTypeName);

        assertTrue(actual.isPresent());
        assertEquals(trainingTypeName, actual.get().getTrainingTypeName());
        assertEquals(expectedId, actual.get().getId());
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

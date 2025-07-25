package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.dataset.DataSet;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingTypeDAOImplTest extends BaseIntegrationTest<TrainingTypeDAOImpl> {

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void findByName_ShouldReturnEmpty_WhenTrainingTypeDoesNotExist() {
        String invalidTraining = "Not_exist_training";

        Optional<TrainingType> actual = dao.findByName(invalidTraining);

        assertThat(actual.isPresent(), is(false));
    }

    @ParameterizedTest
    @MethodSource("provideValidTrainingTypeNames")
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindByName_ShouldReturnCorrectTrainingTypeForValidNames(String trainingTypeName, Long expectedId) {
        Optional<TrainingType> actual = dao.findByName(trainingTypeName);

        assertThat(actual.isPresent(), is(true));
        assertThat(actual.get(), allOf(
                hasProperty("id", is(expectedId)),
                hasProperty("trainingTypeName", is(trainingTypeName))
        ));
    }

    @Test
    @DataSet(value = "dataset/training-test-data.xml", cleanBefore = true, cleanAfter = true, transactional = true, disableConstraints = true)
    void testFindAll_ShouldReturnAllTrainingTypes_Hamcrest() {
        List<TrainingType> trainingTypes = dao.findAll();

        assertThat(trainingTypes, hasSize(7));
        assertThat(trainingTypes, containsInAnyOrder(
                allOf(
                        hasProperty("id", is(1L)),
                        hasProperty("trainingTypeName", is("Strength"))
                ),
                allOf(
                        hasProperty("id", is(2L)),
                        hasProperty("trainingTypeName", is("Yoga"))
                ),
                allOf(
                        hasProperty("id", is(3L)),
                        hasProperty("trainingTypeName", is("Cardio"))
                ),
                allOf(
                        hasProperty("id", is(4L)),
                        hasProperty("trainingTypeName", is("HIIT"))
                ),
                allOf(
                        hasProperty("id", is(5L)),
                        hasProperty("trainingTypeName", is("Pilates"))
                ),
                allOf(
                        hasProperty("id", is(6L)),
                        hasProperty("trainingTypeName", is("Flexibility"))
                ),
                allOf(
                        hasProperty("id", is(7L)),
                        hasProperty("trainingTypeName", is("Boxing"))
                )
        ));
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

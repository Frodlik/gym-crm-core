package com.gym.crm.actuator.prometheus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingMetricsTest {
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter trainingCreated;
    @Mock
    private Counter trainingRetrieved;

    private TrainingMetrics trainingMetrics;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("gym.training.created.count",
                "description", "Number of trainings created"))
                .thenReturn(trainingCreated);
        when(meterRegistry.counter("gym.training.retrieved.count",
                "description", "Number of training searches performed"))
                .thenReturn(trainingRetrieved);

        trainingMetrics = new TrainingMetrics(meterRegistry);
    }

    @Test
    void recordTrainingCreated_shouldIncrementCreatedCounter() {
        trainingMetrics.recordTrainingCreated();

        verify(trainingCreated).increment();
    }

    @Test
    void recordTrainingRetrieval_shouldIncrementRetrievalCounter() {
        trainingMetrics.recordTrainingRetrieval();

        verify(trainingRetrieved).increment();
    }
}

package com.gym.crm.actuator.prometheus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TrainingMetrics {
    private static final String DESCRIPTION = "description";

    private final Counter trainingCreated;
    private final Counter trainingRetrieved;

    public TrainingMetrics(MeterRegistry meterRegistry) {
        this.trainingCreated = meterRegistry.counter("gym.training.created.count", DESCRIPTION, "Number of trainings created");

        this.trainingRetrieved = meterRegistry.counter("gym.training.retrieved.count", DESCRIPTION, "Number of training searches performed");
    }

    public void recordTrainingCreated() {
        trainingCreated.increment();
    }

    public void recordTrainingRetrieval() {
        trainingRetrieved.increment();
    }
}

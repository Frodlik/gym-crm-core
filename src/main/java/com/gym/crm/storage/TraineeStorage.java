package com.gym.crm.storage;

import com.gym.crm.model.Trainee;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component("traineeStorage")
public class TraineeStorage {
    @Getter
    private final Map<Long, Trainee> trainees = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Long getNextId() {
        return idGenerator.getAndIncrement();
    }
}

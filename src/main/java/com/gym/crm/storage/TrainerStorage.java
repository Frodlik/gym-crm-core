package com.gym.crm.storage;

import com.gym.crm.model.Trainer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component("trainerStorage")
public class TrainerStorage {
    @Getter
    private final Map<Long, Trainer> trainers = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Long getNextId() {
        return idGenerator.getAndIncrement();
    }
}

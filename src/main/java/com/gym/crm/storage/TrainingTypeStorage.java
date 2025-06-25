package com.gym.crm.storage;

import com.gym.crm.model.TrainingType;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("trainingTypeStorage")
public class TrainingTypeStorage {
    @Getter
    private final Map<String, TrainingType> trainingTypes = new ConcurrentHashMap<>();
}

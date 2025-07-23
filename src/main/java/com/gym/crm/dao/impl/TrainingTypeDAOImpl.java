package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingTypeDAO;
import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeDAOImpl implements TrainingTypeDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDAOImpl.class);

    private final TransactionHandler transactionHandler;

    @Override
    public Optional<TrainingType> getByName(String name) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            TrainingType trainingType = entityManager.createQuery("FROM TrainingType tt " +
                            "WHERE tt.trainingTypeName = :name", TrainingType.class)
                    .setParameter("name", name)
                    .getSingleResult();

            logger.info("Retrieved TrainingType by name: {}", name);

            return Optional.ofNullable(trainingType);
        });
    }
}

package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingTypeDAO;
import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeDAOImpl implements TrainingTypeDAO {
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDAOImpl.class);

    private final TransactionHandler transactionHandler;

    @Override
    public Optional<TrainingType> findByName(String name) {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            TrainingType trainingType = entityManager.createQuery("FROM TrainingType tt " +
                            "WHERE tt.trainingTypeName = :name", TrainingType.class)
                    .setParameter("name", name)
                    .getSingleResult();

            logger.info("Retrieved TrainingType by name: {}", name);

            return Optional.ofNullable(trainingType);
        });
    }

    @Override
    public List<TrainingType> findAll() {
        return transactionHandler.performReturningWithinSession(entityManager -> {
            List<TrainingType> trainingTypes = entityManager.createQuery("FROM TrainingType", TrainingType.class)
                    .getResultList();

            logger.info("Retrieved all TrainingTypes, count: {}", trainingTypes.size());

            return trainingTypes;
        });
    }
}

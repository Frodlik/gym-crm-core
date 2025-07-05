package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainingDAOImpl.class);

    private final SessionFactory sessionFactory;

    @Autowired
    public TrainingDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Training create(Training training) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(training);

        log.info("Created Training with ID: {}", training.getId());

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Training training = session.get(Training.class, id);

        log.debug("Training found with ID: {}", id);

        return Optional.ofNullable(training);
    }

    @Override
    public List<Training> findAll() {
        Session session = sessionFactory.getCurrentSession();
        List<Training> trainings = session.createQuery("FROM Training", Training.class).getResultList();

        log.debug("Retrieved all trainings. Count: {}", trainings.size());

        return trainings;
    }
}

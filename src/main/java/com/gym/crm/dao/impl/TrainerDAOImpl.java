package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDAOImpl implements TrainerDAO {
    private static final Logger log = LoggerFactory.getLogger(TrainerDAOImpl.class);

    private final SessionFactory sessionFactory;

    @Autowired
    public TrainerDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainer create(Trainer trainer) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(trainer);

        log.info("Created Trainer with ID: {}", trainer.getId());

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Trainer trainer = session.get(Trainer.class, id);

        log.debug("Trainer found with ID: {}", id);

        return Optional.ofNullable(trainer);
    }

    @Override
    public List<Trainer> findAll() {
        Session session = sessionFactory.getCurrentSession();
        List<Trainer> trainers = session.createQuery("FROM Trainer", Trainer.class).getResultList();

        log.debug("Retrieved all trainers. Count: {}", trainers.size());

        return trainers;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Session session = sessionFactory.getCurrentSession();

        Trainer existingTrainer = session.get(Trainer.class, trainer.getId());
        if (existingTrainer == null) {
            throw new DaoException("Trainer not found with ID: " + trainer.getId());
        }

        Trainer updatedTrainer = session.merge(trainer);

        log.info("Trainer updated with ID: {}", trainer.getId());

        return updatedTrainer;
    }
}

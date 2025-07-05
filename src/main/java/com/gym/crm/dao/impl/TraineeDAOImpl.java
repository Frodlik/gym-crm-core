package com.gym.crm.dao.impl;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.exception.DaoException;
import com.gym.crm.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDAOImpl implements TraineeDAO {
    private static final Logger log = LoggerFactory.getLogger(TraineeDAOImpl.class);

    private final SessionFactory sessionFactory;

    @Autowired
    public TraineeDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainee create(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(trainee);

        log.info("Created Trainee with ID: {}", trainee.getId());

        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = session.get(Trainee.class, id);

        log.debug("Found trainee with ID: {}", id);

        return Optional.ofNullable(trainee);
    }

    @Override
    public List<Trainee> findAll() {
        Session session = sessionFactory.getCurrentSession();
        List<Trainee> trainees = session.createQuery("FROM Trainee", Trainee.class).getResultList();

        log.debug("Retrieved all trainees. Count: {}", trainees.size());

        return trainees;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Session session = sessionFactory.getCurrentSession();

        Trainee existingTrainee = session.get(Trainee.class, trainee.getId());
        if (existingTrainee == null) {
            throw new DaoException("Trainee not found with ID: " + trainee.getId());
        }

        Trainee updatedTrainee = session.merge(trainee);

        log.info("Trainee updated with ID: {}", trainee.getId());

        return updatedTrainee;
    }

    @Override
    public boolean delete(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Trainee trainee = session.get(Trainee.class, id);

        if (trainee != null) {
            session.remove(trainee);
            log.info("Trainee deleted with ID: {}", id);
            return true;
        }

        log.warn("Trainee not found for deletion with ID: {}", id);

        return false;
    }
}

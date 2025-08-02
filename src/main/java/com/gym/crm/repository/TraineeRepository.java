package com.gym.crm.repository;

import com.gym.crm.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findTraineeByUser_Username(String username);

    @Transactional
    void deleteByUser_Username(String username);
}

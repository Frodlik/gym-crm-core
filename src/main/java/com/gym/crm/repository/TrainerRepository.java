package com.gym.crm.repository;

import com.gym.crm.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @Query("SELECT t FROM Trainer t WHERE t.user.username = :username")
    Optional<Trainer> findByUsername(@Param("username") String username);

    @Query("""
        SELECT t FROM Trainer t 
        WHERE t.user.isActive = true 
        AND t NOT IN (
            SELECT tr FROM Trainee trainee 
            JOIN trainee.trainers tr 
            WHERE trainee.user.username = :traineeUsername
        )
        """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);

}

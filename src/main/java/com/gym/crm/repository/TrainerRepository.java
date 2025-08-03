package com.gym.crm.repository;

import com.gym.crm.model.Trainer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @EntityGraph(attributePaths = {
            "user",
            "trainings.trainingType"
    })
    Optional<Trainer> findTrainerByUser_Username(String username);

    @Query("""
                SELECT DISTINCT t FROM Trainer t 
                JOIN FETCH t.user
                LEFT JOIN FETCH t.trainings trn
                LEFT JOIN FETCH trn.trainingType
                WHERE t.user.isActive = true 
                AND t NOT IN (
                    SELECT tr FROM Trainee trainee 
                    JOIN trainee.trainers tr 
                    WHERE trainee.user.username = :traineeUsername
                )
            """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);

    @EntityGraph(attributePaths = {
            "user",
            "trainings.trainingType"
    })
    List<Trainer> findAllByUser_UsernameIn(List<String> usernames);
}

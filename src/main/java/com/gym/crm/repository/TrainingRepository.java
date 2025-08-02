package com.gym.crm.repository;

import com.gym.crm.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query("""
                SELECT t FROM Training t
                JOIN FETCH t.trainingType
                JOIN FETCH t.trainer tr
                JOIN FETCH tr.user
                JOIN FETCH t.trainee tn
                JOIN FETCH tn.user
                WHERE t.id = :id
            """)
    Optional<Training> findById(@Param("id") Long id);

    @Query("""
                SELECT t FROM Training t
                JOIN FETCH t.trainingType
                JOIN FETCH t.trainer tr
                JOIN FETCH tr.user
                JOIN FETCH t.trainee tn
                JOIN FETCH tn.user
            """)
    List<Training> findAll();

    @Query("""
            SELECT DISTINCT t FROM Training t 
            LEFT JOIN FETCH t.trainee trainee
            LEFT JOIN FETCH trainee.user
            LEFT JOIN FETCH t.trainer trainer
            LEFT JOIN FETCH trainer.user
            LEFT JOIN FETCH t.trainingType
            WHERE t.trainee.user.username = :traineeUsername
            AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
            AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            AND (:trainerName IS NULL OR LOWER(CONCAT(t.trainer.user.firstName, ' ', t.trainer.user.lastName)) LIKE LOWER(CONCAT('%', :trainerName, '%')))
            AND (:trainingType IS NULL OR LOWER(t.trainingType.trainingTypeName) LIKE LOWER(CONCAT('%', :trainingType, '%')))
            ORDER BY t.trainingDate DESC
            """)
    List<Training> findTraineeTrainingsByCriteria(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") String trainingType
    );

    @Query("""
            SELECT DISTINCT t FROM Training t 
            LEFT JOIN FETCH t.trainee trainee
            LEFT JOIN FETCH trainee.user
            LEFT JOIN FETCH t.trainer trainer
            LEFT JOIN FETCH trainer.user
            LEFT JOIN FETCH t.trainingType
            WHERE t.trainer.user.username = :trainerUsername
            AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
            AND (:toDate IS NULL OR t.trainingDate <= :toDate)
            AND (:traineeName IS NULL OR LOWER(CONCAT(t.trainee.user.firstName, ' ', t.trainee.user.lastName)) LIKE LOWER(CONCAT('%', :traineeName, '%')))
            ORDER BY t.trainingDate DESC
            """)
    List<Training> findTrainerTrainingsByCriteria(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName
    );
}

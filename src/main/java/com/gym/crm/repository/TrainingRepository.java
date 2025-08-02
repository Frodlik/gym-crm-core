package com.gym.crm.repository;

import com.gym.crm.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {
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
}

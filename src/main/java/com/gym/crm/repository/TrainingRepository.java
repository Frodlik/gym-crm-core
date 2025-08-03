package com.gym.crm.repository;

import com.gym.crm.model.Training;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {
    @EntityGraph(attributePaths = {
            "trainingType",
            "trainer.user",
            "trainee.user"
    })
    Optional<Training> findById(Long id);

    @EntityGraph(attributePaths = {
            "trainingType",
            "trainer.user",
            "trainee.user"
    })
    List<Training> findAll();
}

package com.gym.crm.repository.specification;

import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public final class TrainingSpecifications {
    private static final String TRAINEE = "trainee";
    private static final String TRAINER = "trainer";
    private static final String TRAINING_DATE = "trainingDate";
    private static final String USER = "user";
    private static final String TRAINING_TYPE = "trainingType";
    private static final Logger log = LoggerFactory.getLogger(TrainingSpecifications.class);

    private TrainingSpecifications() {
    }

    public static Specification<Training> forTraineeSearch(TraineeSearchFilter filter) {
        return Specification
                .where(hasTraineeUsername(filter.getTraineeUsername()))
                .and(hasTrainingDateBetween(filter.getFromDate(), filter.getToDate()))
                .and(hasTrainerNameContaining(filter.getTrainerName()))
                .and(hasTrainingTypeContaining(filter.getTrainingType()))
                .and(withEagerFetching())
                .and(orderByTrainingDateDesc());
    }

    public static Specification<Training> forTrainerSearch(TrainerSearchFilter filter) {
        return Specification
                .where(hasTrainerUsername(filter.getTrainerUsername()))
                .and(hasTrainingDateBetween(filter.getFromDate(), filter.getToDate()))
                .and(hasTraineeNameContaining(filter.getTraineeName()))
                .and(withEagerFetching())
                .and(orderByTrainingDateDesc());
    }

    private static Specification<Training> hasTraineeUsername(String traineeUsername) {
        return (root, query, criteriaBuilder) -> {
            if (isBlankOrNull(traineeUsername)) {
                return criteriaBuilder.conjunction();
            }

            addEagerFetching(root, query);

            Join<Training, Trainee> traineeJoin = root.join(TRAINEE, JoinType.LEFT);
            Join<Trainee, User> userJoin = traineeJoin.join(USER, JoinType.LEFT);

            return criteriaBuilder.equal(userJoin.get("username"), traineeUsername);
        };
    }

    private static Specification<Training> hasTrainerUsername(String trainerUsername) {
        return (root, query, criteriaBuilder) -> {
            if (isBlankOrNull(trainerUsername)) {
                return criteriaBuilder.conjunction();
            }

            addEagerFetching(root, query);

            Join<Training, Trainer> trainerJoin = root.join(TRAINER, JoinType.LEFT);
            Join<Trainer, User> userJoin = trainerJoin.join(USER, JoinType.LEFT);

            return criteriaBuilder.equal(userJoin.get("username"), trainerUsername);
        };
    }

    private static Specification<Training> hasTrainingDateBetween(LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            addEagerFetching(root, query);

            Predicate predicate = criteriaBuilder.conjunction();

            if (fromDate != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get(TRAINING_DATE), fromDate));
            }

            if (toDate != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get(TRAINING_DATE), toDate));
            }

            return predicate;
        };
    }

    private static Specification<Training> hasTrainerNameContaining(String trainerName) {
        return (root, query, criteriaBuilder) -> {
            if (isBlankOrNull(trainerName)) {
                return criteriaBuilder.conjunction();
            }

            addEagerFetching(root, query);

            Join<Training, Trainer> trainerJoin = root.join(TRAINER, JoinType.LEFT);
            Join<Trainer, User> userJoin = trainerJoin.join(USER, JoinType.LEFT);

            Expression<String> fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(userJoin.get("firstName"), " "),
                    userJoin.get("lastName")
            );

            return criteriaBuilder.like(
                    criteriaBuilder.lower(fullName),
                    "%" + trainerName.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Training> hasTraineeNameContaining(String traineeName) {
        return (root, query, criteriaBuilder) -> {
            if (isBlankOrNull(traineeName)) {
                return criteriaBuilder.conjunction();
            }

            addEagerFetching(root, query);

            Join<Training, Trainee> traineeJoin = root.join(TRAINEE, JoinType.LEFT);
            Join<Trainee, User> userJoin = traineeJoin.join(USER, JoinType.LEFT);

            Expression<String> fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(userJoin.get("firstName"), " "),
                    userJoin.get("lastName")
            );

            return criteriaBuilder.like(
                    criteriaBuilder.lower(fullName),
                    "%" + traineeName.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Training> hasTrainingTypeContaining(String trainingType) {
        return (root, query, criteriaBuilder) -> {
            if (isBlankOrNull(trainingType)) {
                return criteriaBuilder.conjunction();
            }

            addEagerFetching(root, query);

            Join<Training, TrainingType> trainingTypeJoin = root.join(TRAINING_TYPE, JoinType.LEFT);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(trainingTypeJoin.get("trainingTypeName")),
                    "%" + trainingType.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Training> withEagerFetching() {
        return (root, query, criteriaBuilder) -> {
            addEagerFetching(root, query);

            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<Training> orderByTrainingDateDesc() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get(TRAINING_DATE)));

            return criteriaBuilder.conjunction();
        };
    }

    private static void addEagerFetching(Root<Training> root, CriteriaQuery<?> query) {
        if (query.getResultType() == Long.class || query.getResultType() == long.class) {
            return;
        }

        try {
            root.fetch(TRAINEE, JoinType.LEFT).fetch(USER, JoinType.LEFT);
            root.fetch(TRAINER, JoinType.LEFT).fetch(USER, JoinType.LEFT);
            root.fetch(TRAINING_TYPE, JoinType.LEFT);
            query.distinct(true);
        } catch (IllegalStateException e) {
            log.warn("Eager fetching failed: {}", e.getMessage());
        }
    }

    private static boolean isBlankOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.gym.crm.dao.criteria;

import com.gym.crm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrainingCriteriaBuilder {
    private static final String TRAINEE = "trainee";
    private static final String TRAINER = "trainer";
    private static final String USER = "user";
    private static final String TRAINING_TYPE = "trainingType";
    private static final String USERNAME = "username";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String TRAINING_DATE = "trainingDate";
    private static final String TRAINING_TYPE_NAME = "trainingTypeName";

    public List<Training> findTrainingsByCriteria(
            EntityManager entityManager,
            TrainingSearchCriteria criteria
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = buildTrainingCriteriaQuery(cb, criteria);

        return entityManager.createQuery(query).getResultList();
    }

    private CriteriaQuery<Training> buildTrainingCriteriaQuery(
            CriteriaBuilder cb,
            TrainingSearchCriteria criteria
    ) {
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> root = query.from(Training.class);

        TrainingJoins joins = createJoins(root, criteria.trainingType());
        List<Predicate> predicates = buildPredicates(cb, root, joins, criteria);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get(TRAINING_DATE)));

        return query;
    }

    private TrainingJoins createJoins(Root<Training> root, String trainingType) {
        Join<Object, Object> trainerUserJoin = root.join(TRAINER, JoinType.LEFT)
                .join(USER, JoinType.LEFT);
        Join<Object, Object> traineeUserJoin = root.join(TRAINEE, JoinType.LEFT)
                .join(USER, JoinType.LEFT);

        Join<Object, Object> trainingTypeJoin = StringUtils.hasText(trainingType)
                ? root.join(TRAINING_TYPE, JoinType.LEFT)
                : null;

        return new TrainingJoins(trainerUserJoin, traineeUserJoin, trainingTypeJoin);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Training> root,
            TrainingJoins joins,
            TrainingSearchCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();

        addUsernamePredicate(cb, predicates, joins, criteria);
        addDateRangePredicates(cb, predicates, root, criteria);
        addNameFilterPredicate(cb, predicates, joins, criteria);
        addTrainingTypePredicate(cb, predicates, joins, criteria);

        return predicates;
    }

    private void addUsernamePredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            TrainingJoins joins,
            TrainingSearchCriteria criteria
    ) {
        if (!StringUtils.hasText(criteria.userUsername())) {
            return;
        }

        Join<Object, Object> userJoin = isTraineeRole(criteria.userRole())
                ? joins.traineeUserJoin()
                : joins.trainerUserJoin();

        predicates.add(cb.equal(userJoin.get(USERNAME), criteria.userUsername()));
    }

    private void addDateRangePredicates(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            Root<Training> root,
            TrainingSearchCriteria criteria
    ) {
        Optional.ofNullable(criteria.fromDate())
                .ifPresent(date -> predicates.add(
                        cb.greaterThanOrEqualTo(root.get(TRAINING_DATE), date)));

        Optional.ofNullable(criteria.toDate())
                .ifPresent(date -> predicates.add(
                        cb.lessThanOrEqualTo(root.get(TRAINING_DATE), date)));
    }

    private void addNameFilterPredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            TrainingJoins joins,
            TrainingSearchCriteria criteria
    ) {
        if (!StringUtils.hasText(criteria.nameFilter())) {
            return;
        }

        Join<Object, Object> userJoin = criteria.isSearchingByTrainer()
                ? joins.traineeUserJoin()
                : joins.trainerUserJoin();

        String pattern = "%" + criteria.nameFilter().toLowerCase() + "%";

        Predicate namePredicate = cb.or(
                cb.like(cb.lower(userJoin.get(FIRST_NAME)), pattern),
                cb.like(cb.lower(userJoin.get(LAST_NAME)), pattern),
                cb.like(cb.lower(cb.concat(
                        cb.concat(userJoin.get(FIRST_NAME), " "),
                        userJoin.get(LAST_NAME))), pattern)
        );

        predicates.add(namePredicate);
    }

    private void addTrainingTypePredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            TrainingJoins joins,
            TrainingSearchCriteria criteria
    ) {
        if (!StringUtils.hasText(criteria.trainingType()) || joins.trainingTypeJoin() == null) {
            return;
        }

        predicates.add(cb.like(
                cb.lower(joins.trainingTypeJoin().get(TRAINING_TYPE_NAME)),
                "%" + criteria.trainingType().toLowerCase() + "%"
        ));
    }

    private boolean isTraineeRole(String userRole) {
        return TRAINEE.equalsIgnoreCase(userRole);
    }

    private record TrainingJoins(
            Join<Object, Object> trainerUserJoin,
            Join<Object, Object> traineeUserJoin,
            Join<Object, Object> trainingTypeJoin
    ) {
    }
}

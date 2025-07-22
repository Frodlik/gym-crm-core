package com.gym.crm.dao.criteria;

import java.time.LocalDate;

public record TrainingSearchCriteria(
        String userUsername,
        String userRole,
        LocalDate fromDate,
        LocalDate toDate,
        String nameFilter,
        boolean isSearchingByTrainer,
        String trainingType
) {

    public static TrainingSearchCriteriaBuilder builder() {
        return new TrainingSearchCriteriaBuilder();
    }

    public static class TrainingSearchCriteriaBuilder {
        private String userUsername;
        private String userRole;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String nameFilter;
        private boolean isSearchingByTrainer;
        private String trainingType;

        public TrainingSearchCriteriaBuilder userUsername(String userUsername) {
            this.userUsername = userUsername;

            return this;
        }

        public TrainingSearchCriteriaBuilder userRole(String userRole) {
            this.userRole = userRole;

            return this;
        }

        public TrainingSearchCriteriaBuilder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;

            return this;
        }

        public TrainingSearchCriteriaBuilder toDate(LocalDate toDate) {
            this.toDate = toDate;

            return this;
        }

        public TrainingSearchCriteriaBuilder nameFilter(String nameFilter) {
            this.nameFilter = nameFilter;

            return this;
        }

        public TrainingSearchCriteriaBuilder isSearchingByTrainer(boolean isSearchingByTrainer) {
            this.isSearchingByTrainer = isSearchingByTrainer;

            return this;
        }

        public TrainingSearchCriteriaBuilder trainingType(String trainingType) {
            this.trainingType = trainingType;

            return this;
        }

        public TrainingSearchCriteria build() {
            return new TrainingSearchCriteria(
                    userUsername, userRole, fromDate, toDate,
                    nameFilter, isSearchingByTrainer, trainingType
            );
        }
    }
}

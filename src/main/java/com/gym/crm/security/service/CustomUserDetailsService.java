package com.gym.crm.security.service;

import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public CustomUserDetailsService(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findUserByType(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private Optional<UserDetails> findUserByType(String username) {
        return findTrainee(username)
                .or(() -> findTrainer(username));
    }

    private Optional<UserDetails> findTrainee(String username) {
        return traineeRepository.findTraineeByUser_Username(username)
                .map(trainee -> createUserDetails(trainee.getUser(), "TRAINEE"));
    }

    private Optional<UserDetails> findTrainer(String username) {
        return trainerRepository.findTrainerByUser_Username(username)
                .map(trainer -> createUserDetails(trainer.getUser(), "TRAINER"));
    }

    private CustomUserDetails createUserDetails(User user, String role) {
        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.getIsActive(),
                role
        );
    }
}

package com.gym.crm.security;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
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
        Optional<Trainee> trainee = traineeRepository.findTraineeByUser_Username(username);
        if (trainee.isPresent()) {
            return new CustomUserDetails(
                    trainee.get().getUser().getUsername(),
                    trainee.get().getUser().getPassword(),
                    trainee.get().getUser().getIsActive(),
                    "TRAINEE"
            );
        }

        Optional<Trainer> trainer = trainerRepository.findTrainerByUser_Username(username);
        if (trainer.isPresent()) {
            return new CustomUserDetails(
                    trainer.get().getUser().getUsername(),
                    trainer.get().getUser().getPassword(),
                    trainer.get().getUser().getIsActive(),
                    "TRAINER"
            );
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}

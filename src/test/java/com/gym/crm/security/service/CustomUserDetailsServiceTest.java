package com.gym.crm.security.service;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    private static final String USERNAME = "arc.warden";
    private static final String PASSWORD = "superPassword123";
    private static final String FIRST_NAME = "Arc";
    private static final String LAST_NAME = "Warden";

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testLoadUserByUsername_whenTraineeExists_shouldReturnTraineeUserDetails() {
        Trainee trainee = buildTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));

        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());

        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertEquals("TRAINEE", customUserDetails.getRole());
    }

    @Test
    void testLoadUserByUsername_whenTrainerExists_shouldReturnTrainerUserDetails() {
        Trainer trainer = buildTrainer();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));

        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(trainerRepository).findTrainerByUser_Username(USERNAME);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertTrue(result.isEnabled());

        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertEquals("TRAINER", customUserDetails.getRole());
    }

    @Test
    void testLoadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.empty());
        when(trainerRepository.findTrainerByUser_Username(USERNAME)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(USERNAME));

        assertEquals("User not found with username: " + USERNAME, exception.getMessage());
        verify(traineeRepository).findTraineeByUser_Username(USERNAME);
        verify(trainerRepository).findTrainerByUser_Username(USERNAME);
    }

    @Test
    void testLoadUserByUsername_whenInactiveTraineeExists_shouldReturnDisabledUserDetails() {
        Trainee inactiveTrainee = buildInactiveTrainee();

        when(traineeRepository.findTraineeByUser_Username(USERNAME)).thenReturn(Optional.of(inactiveTrainee));

        UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertFalse(result.isEnabled());

        CustomUserDetails customUserDetails = (CustomUserDetails) result;
        assertEquals("TRAINEE", customUserDetails.getRole());
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private Trainee buildInactiveTrainee() {
        User user = User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(false)
                .build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(1L)
                .user(user)
                .build();
    }
}

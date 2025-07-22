package com.gym.crm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ActivationStatusRequest;
import com.gym.crm.openapi.model.AvailableTrainerGetResponse;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gym.crm.openapi.model.TraineeCreateRequest;
import com.gym.crm.openapi.model.TraineeCreateResponse;
import com.gym.crm.openapi.model.TraineeGetResponse;
import com.gym.crm.openapi.model.TraineeTrainingGetResponse;
import com.gym.crm.openapi.model.TraineeUpdateRequest;
import com.gym.crm.openapi.model.TraineeUpdateResponse;
import com.gym.crm.openapi.model.Trainer;
import com.gym.crm.util.JsonReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.gym.crm.controller.ApiConstant.BASE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {
    private static final String TRAINEE_USERNAME = "naruto.uzumaki";
    private static final String TRAINEE_FIRST_NAME = "Naruto";
    private static final String TRAINEE_LAST_NAME = "Uzumaki";
    private static final String TRAINEE_ADDRESS = "Hidden Leaf Village, Konoha";

    private static final String TRAINER_1_USERNAME = "sakura.haruno";
    private static final String TRAINER_1_FIRST_NAME = "Sakura";
    private static final String TRAINER_1_LAST_NAME = "Haruno";
    private static final String TRAINER_1_SPECIALIZATION = "Medical Ninjutsu";
    private static final String TRAINER_2_USERNAME = "sasuke.uchiha";
    private static final String TRAINER_2_FIRST_NAME = "Sasuke";
    private static final String TRAINER_2_LAST_NAME = "Uchiha";
    private static final String TRAINER_2_SPECIALIZATION = "Sharingan Training";

    private static final String TRAINING_NAME_MEDICAL = "Medical Ninjutsu";
    private static final String TRAINING_NAME_SHARINGAN = "Sharingan Training";
    private static final int TRAINING_DURATION_MEDICAL = 120;
    private static final int TRAINING_DURATION_SHARINGAN = 90;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;
    @InjectMocks
    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController).build();
    }

    @Test
    void testRegisterTraineeSuccess() throws Exception {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName(TRAINEE_FIRST_NAME);
        request.setLastName(TRAINEE_LAST_NAME);
        request.setDateOfBirth(java.time.LocalDate.of(1999, 10, 10));
        request.setAddress(TRAINEE_ADDRESS);

        TraineeCreateResponse response = new TraineeCreateResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setPassword("hokage123");

        when(gymFacade.createTrainee(any())).thenReturn(response);

        var result = mockMvc.perform(post(BASE_PATH + "/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINEE_USERNAME))
                .andExpect(jsonPath("$.password").value("hokage123"));

        verify(gymFacade).createTrainee(any(TraineeCreateRequest.class));
    }

    @Test
    void testGetTraineeProfileSuccess() throws Exception {
        TraineeGetResponse profile = new TraineeGetResponse();
        profile.setFirstName(TRAINEE_FIRST_NAME);
        profile.setLastName(TRAINEE_LAST_NAME);
        profile.setAddress(TRAINEE_ADDRESS);
        profile.setDateOfBirth(java.time.LocalDate.of(1999, 10, 10));
        profile.setIsActive(true);

        when(gymFacade.getTraineeByUsername(TRAINEE_USERNAME)).thenReturn(profile);

        var result = mockMvc.perform(get(BASE_PATH + "/trainees/" + TRAINEE_USERNAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(TRAINEE_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TRAINEE_LAST_NAME))
                .andExpect(jsonPath("$.address").value(TRAINEE_ADDRESS))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(gymFacade).getTraineeByUsername(TRAINEE_USERNAME);
    }

    @Test
    void testUpdateTraineeProfileSuccess() throws Exception {
        TraineeUpdateRequest request = JsonReaderUtil.readFromJson(
                "json/trainee-update-request.json",
                TraineeUpdateRequest.class
        );

        TraineeUpdateResponse response = new TraineeUpdateResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setDateOfBirth(request.getDateOfBirth());
        response.setAddress(request.getAddress());
        response.setIsActive(request.getIsActive());

        when(gymFacade.updateTrainee(eq(TRAINEE_USERNAME), any())).thenReturn(response);

        var result = mockMvc.perform(put(BASE_PATH + "/trainees/" + TRAINEE_USERNAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINEE_USERNAME))
                .andExpect(jsonPath("$.firstName").value("Naruto"))
                .andExpect(jsonPath("$.lastName").value("Hokage"))
                .andExpect(jsonPath("$.address").value("Hokage Office, Konoha"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(gymFacade).updateTrainee(eq(TRAINEE_USERNAME), any(TraineeUpdateRequest.class));
    }

    @Test
    void testDeleteTraineeProfileSuccess() throws Exception {
        var result = mockMvc.perform(delete(BASE_PATH + "/trainees/" + TRAINEE_USERNAME));

        result.andExpect(status().isOk());

        verify(gymFacade).deleteTrainee(TRAINEE_USERNAME);
    }

    @Test
    void testChangeActivationStatusSuccess() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest();
        request.setIsActive(false);

        var result = mockMvc.perform(patch(BASE_PATH + "/trainees/" + TRAINEE_USERNAME + "/change-activation-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).toggleTraineeActivation(TRAINEE_USERNAME, false);
    }

    @Test
    void testGetAvailableTrainersSuccess() throws Exception {
        List<AvailableTrainerGetResponse> trainers = JsonReaderUtil.readFromJson(
                "json/available-trainers-response.json",
                new TypeReference<>() {
                }
        );

        when(gymFacade.getTrainersNotAssignedToTrainee(TRAINEE_USERNAME)).thenReturn(trainers);

        var result = mockMvc.perform(get(BASE_PATH + "/trainees/" + TRAINEE_USERNAME + "/available-trainers"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(TRAINER_1_USERNAME))
                .andExpect(jsonPath("$[0].firstName").value(TRAINER_1_FIRST_NAME))
                .andExpect(jsonPath("$[0].lastName").value(TRAINER_1_LAST_NAME))
                .andExpect(jsonPath("$[0].specialization").value(TRAINER_1_SPECIALIZATION))
                .andExpect(jsonPath("$[1].username").value(TRAINER_2_USERNAME))
                .andExpect(jsonPath("$[1].firstName").value(TRAINER_2_FIRST_NAME))
                .andExpect(jsonPath("$[1].lastName").value(TRAINER_2_LAST_NAME))
                .andExpect(jsonPath("$[1].specialization").value(TRAINER_2_SPECIALIZATION));

        verify(gymFacade).getTrainersNotAssignedToTrainee(TRAINEE_USERNAME);
    }

    @Test
    void testUpdateTraineeTrainersSuccess() throws Exception {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest();
        request.setTrainerUsernames(List.of("sakura.haruno", "sasuke.uchiha"));

        TraineeAssignedTrainersUpdateResponse response = new TraineeAssignedTrainersUpdateResponse();
        List<AvailableTrainerGetResponse> availableTrainers = JsonReaderUtil.readFromJson(
                "json/available-trainers-response.json",
                new TypeReference<>() {
                }
        );

        response.setTrainers(availableTrainers.stream()
                .map(this::convertToTrainer)
                .toList());

        when(gymFacade.updateTraineeTrainersList(eq(TRAINEE_USERNAME), any())).thenReturn(response);

        var result = mockMvc.perform(put(BASE_PATH + "/trainees/" + TRAINEE_USERNAME + "/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers[0].username").value(TRAINER_1_USERNAME))
                .andExpect(jsonPath("$.trainers[0].firstName").value(TRAINER_1_FIRST_NAME))
                .andExpect(jsonPath("$.trainers[0].lastName").value(TRAINER_1_LAST_NAME))
                .andExpect(jsonPath("$.trainers[0].specialization").value(TRAINER_1_SPECIALIZATION))
                .andExpect(jsonPath("$.trainers[1].username").value(TRAINER_2_USERNAME))
                .andExpect(jsonPath("$.trainers[1].firstName").value(TRAINER_2_FIRST_NAME))
                .andExpect(jsonPath("$.trainers[1].lastName").value(TRAINER_2_LAST_NAME))
                .andExpect(jsonPath("$.trainers[1].specialization").value(TRAINER_2_SPECIALIZATION));

        verify(gymFacade).updateTraineeTrainersList(eq(TRAINEE_USERNAME), any());
    }

    @Test
    void testGetTraineeTrainingsSuccess() throws Exception {
        List<TraineeTrainingGetResponse> trainings = JsonReaderUtil.readFromJson(
                "json/get-trainee-trainings-response.json",
                new TypeReference<>() {
                }
        );

        when(gymFacade.getTraineeTrainingsByCriteria(any())).thenReturn(trainings);

        var result = mockMvc.perform(get(BASE_PATH + "/trainees/" + TRAINEE_USERNAME + "/trainings")
                .param("fromDate", "2025-07-01")
                .param("toDate", "2025-07-31")
                .param("trainerName", TRAINER_1_FIRST_NAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value(TRAINING_NAME_MEDICAL))
                .andExpect(jsonPath("$[0].trainingDate[0]").value(2025))
                .andExpect(jsonPath("$[0].trainingDate[1]").value(7))
                .andExpect(jsonPath("$[0].trainingDate[2]").value(15))
                .andExpect(jsonPath("$[0].trainerName").value(TRAINER_1_FIRST_NAME))
                .andExpect(jsonPath("$[0].trainingDuration").value(TRAINING_DURATION_MEDICAL))
                .andExpect(jsonPath("$[1].trainingName").value(TRAINING_NAME_SHARINGAN))
                .andExpect(jsonPath("$[1].trainingDate[0]").value(2025))
                .andExpect(jsonPath("$[1].trainingDate[1]").value(7))
                .andExpect(jsonPath("$[1].trainingDate[2]").value(20))
                .andExpect(jsonPath("$[1].trainerName").value(TRAINER_2_FIRST_NAME))
                .andExpect(jsonPath("$[1].trainingDuration").value(TRAINING_DURATION_SHARINGAN));

        verify(gymFacade).getTraineeTrainingsByCriteria(any());
    }

    @Test
    void testGetTraineeTrainingsInvalidDate() throws Exception {
        var result = mockMvc.perform(get(BASE_PATH + "/trainees/" + TRAINEE_USERNAME + "/trainings")
                .param("fromDate", "not-a-date"));

        result.andExpect(status().isBadRequest());
    }

    private Trainer convertToTrainer(AvailableTrainerGetResponse response) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(response.getFirstName());
        trainer.setLastName(response.getLastName());
        trainer.setSpecialization(response.getSpecialization());
        trainer.setUsername(response.getUsername());

        return trainer;
    }
}

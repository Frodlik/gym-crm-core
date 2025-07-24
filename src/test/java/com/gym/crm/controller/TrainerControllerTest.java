package com.gym.crm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ActivationStatusRequest;
import com.gym.crm.openapi.model.TrainerCreateRequest;
import com.gym.crm.openapi.model.TrainerCreateResponse;
import com.gym.crm.openapi.model.TrainerGetResponse;
import com.gym.crm.openapi.model.TrainerTrainingGetResponse;
import com.gym.crm.openapi.model.TrainerUpdateRequest;
import com.gym.crm.openapi.model.TrainerUpdateResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {
    private static final String TRAINER_USERNAME = "kakashi.hatake";
    private static final String TRAINER_FIRST_NAME = "Kakashi";
    private static final String TRAINER_LAST_NAME = "Hatake";
    private static final String TRAINER_SPECIALIZATION = "Sharingan Techniques";

    private static final String TRAINEE_1_NAME = "Naruto";
    private static final String TRAINEE_2_NAME = "Sasuke";

    private static final String TRAINING_NAME_SHADOW_CLONE = "Shadow Clone Jutsu";
    private static final String TRAINING_NAME_CHIDORI = "Chidori Training";
    private static final int TRAINING_DURATION_SHADOW_CLONE = 180;
    private static final int TRAINING_DURATION_CHIDORI = 150;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;
    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
    }

    @Test
    void testRegisterTrainerSuccess() throws Exception {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setFirstName(TRAINER_FIRST_NAME);
        request.setLastName(TRAINER_LAST_NAME);
        request.setSpecialization(TRAINER_SPECIALIZATION);

        TrainerCreateResponse response = new TrainerCreateResponse();
        response.setUsername(TRAINER_USERNAME);
        response.setPassword("sensei123");

        when(gymFacade.createTrainer(any())).thenReturn(response);

        var result = mockMvc.perform(post(BASE_PATH + "/trainers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINER_USERNAME))
                .andExpect(jsonPath("$.password").value("sensei123"));

        verify(gymFacade).createTrainer(any(TrainerCreateRequest.class));
    }

    @Test
    void testGetTrainerProfileSuccess() throws Exception {
        TrainerGetResponse profile = new TrainerGetResponse();
        profile.setFirstName(TRAINER_FIRST_NAME);
        profile.setLastName(TRAINER_LAST_NAME);
        profile.setSpecialization(TRAINER_SPECIALIZATION);
        profile.setIsActive(true);

        when(gymFacade.getTrainerByUsername(TRAINER_USERNAME)).thenReturn(profile);

        var result = mockMvc.perform(get(BASE_PATH + "/trainers/" + TRAINER_USERNAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(TRAINER_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TRAINER_LAST_NAME))
                .andExpect(jsonPath("$.specialization").value(TRAINER_SPECIALIZATION))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(gymFacade).getTrainerByUsername(TRAINER_USERNAME);
    }

    @Test
    void testUpdateTrainerProfileSuccess() throws Exception {
        TrainerUpdateRequest request = JsonReaderUtil.readFromJson(
                "json/trainer-update-request.json",
                TrainerUpdateRequest.class
        );

        TrainerUpdateResponse response = new TrainerUpdateResponse();
        response.setUsername(TRAINER_USERNAME);
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setSpecialization(request.getSpecialization());
        response.setIsActive(request.getIsActive());

        when(gymFacade.updateTrainer(eq(TRAINER_USERNAME), any())).thenReturn(response);

        var result = mockMvc.perform(put(BASE_PATH + "/trainers/" + TRAINER_USERNAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINER_USERNAME))
                .andExpect(jsonPath("$.firstName").value("Kakashi"))
                .andExpect(jsonPath("$.lastName").value("Sensei"))
                .andExpect(jsonPath("$.specialization").value("Advanced Combat Techniques"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(gymFacade).updateTrainer(eq(TRAINER_USERNAME), any(TrainerUpdateRequest.class));
    }

    @Test
    void testChangeActivationStatusSuccess() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest();
        request.setIsActive(false);

        var result = mockMvc.perform(patch(BASE_PATH + "/trainers/" + TRAINER_USERNAME + "/change-activation-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).toggleTrainerActivation(TRAINER_USERNAME, false);
    }

    @Test
    void testGetTrainerTrainingsSuccess() throws Exception {
        List<TrainerTrainingGetResponse> trainings = JsonReaderUtil.readFromJson(
                "json/get-trainer-trainings-response.json",
                new TypeReference<>() {
                }
        );

        when(gymFacade.getTrainerTrainingsByCriteria(any())).thenReturn(trainings);

        var result = mockMvc.perform(get(BASE_PATH + "/trainers/" + TRAINER_USERNAME + "/trainings")
                .param("fromDate", "2025-07-01")
                .param("toDate", "2025-07-31")
                .param("traineeName", TRAINEE_1_NAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value(TRAINING_NAME_SHADOW_CLONE))
                .andExpect(jsonPath("$[0].trainingDate[0]").value(2025))
                .andExpect(jsonPath("$[0].trainingDate[1]").value(7))
                .andExpect(jsonPath("$[0].trainingDate[2]").value(16))
                .andExpect(jsonPath("$[0].traineeName").value(TRAINEE_1_NAME))
                .andExpect(jsonPath("$[0].trainingDuration").value(TRAINING_DURATION_SHADOW_CLONE))
                .andExpect(jsonPath("$[1].trainingName").value(TRAINING_NAME_CHIDORI))
                .andExpect(jsonPath("$[1].trainingDate[0]").value(2025))
                .andExpect(jsonPath("$[1].trainingDate[1]").value(7))
                .andExpect(jsonPath("$[1].trainingDate[2]").value(21))
                .andExpect(jsonPath("$[1].traineeName").value(TRAINEE_2_NAME))
                .andExpect(jsonPath("$[1].trainingDuration").value(TRAINING_DURATION_CHIDORI));

        verify(gymFacade).getTrainerTrainingsByCriteria(any());
    }

    @Test
    void testGetTrainerTrainingsWithoutParams() throws Exception {
        List<TrainerTrainingGetResponse> trainings = JsonReaderUtil.readFromJson(
                "json/get-trainer-trainings-response.json",
                new TypeReference<>() {
                }
        );

        when(gymFacade.getTrainerTrainingsByCriteria(any())).thenReturn(trainings);

        var result = mockMvc.perform(get(BASE_PATH + "/trainers/" + TRAINER_USERNAME + "/trainings"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(gymFacade).getTrainerTrainingsByCriteria(any());
    }

    @Test
    void testGetTrainerTrainingsInvalidDate() throws Exception {
        var result = mockMvc.perform(get(BASE_PATH + "/trainers/" + TRAINER_USERNAME + "/trainings")
                .param("fromDate", "not-a-date"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void testGetTrainerTrainingsWithSpecificTrainee() throws Exception {
        List<TrainerTrainingGetResponse> trainings = List.of(
                createTrainerTraining()
        );

        when(gymFacade.getTrainerTrainingsByCriteria(any())).thenReturn(trainings);

        var result = mockMvc.perform(get(BASE_PATH + "/trainers/" + TRAINER_USERNAME + "/trainings")
                .param("traineeName", TRAINEE_1_NAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].traineeName").value(TRAINEE_1_NAME));

        verify(gymFacade).getTrainerTrainingsByCriteria(any());
    }

    private TrainerTrainingGetResponse createTrainerTraining() {
        TrainerTrainingGetResponse training = new TrainerTrainingGetResponse();
        training.setTrainingName(TrainerControllerTest.TRAINING_NAME_SHADOW_CLONE);
        training.setTrainingDate(java.time.LocalDate.now());
        training.setTraineeName(TrainerControllerTest.TRAINEE_1_NAME);
        training.setTrainingDuration(TrainerControllerTest.TRAINING_DURATION_SHADOW_CLONE);

        return training;
    }
}

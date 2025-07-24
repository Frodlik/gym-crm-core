package com.gym.crm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.TrainingCreateRequest;
import com.gym.crm.openapi.model.TrainingTypeGetResponse;
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

import java.time.LocalDate;
import java.util.List;

import static com.gym.crm.controller.ApiConstant.BASE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {
    private static final String TRAINER_USERNAME = "kakashi.hatake";
    private static final String TRAINEE_USERNAME = "naruto.uzumaki";
    private static final String TRAINING_NAME = "Shadow Clone Jutsu";
    private static final int TRAINING_DURATION = 180;

    private static final String TRAINING_TYPE_1 = "Ninjutsu";
    private static final String TRAINING_TYPE_2 = "Taijutsu";
    private static final String TRAINING_TYPE_3 = "Genjutsu";

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;
    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController).build();
    }

    @Test
    void testCreateTrainingSuccess() throws Exception {
        TrainingCreateRequest request = JsonReaderUtil.readFromJson(
                "json/training-create-request.json",
                TrainingCreateRequest.class
        );

        var result = mockMvc.perform(post(BASE_PATH + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void testCreateTrainingWithMinimalData() throws Exception {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setTraineeUsername(TRAINEE_USERNAME);
        request.setTrainingName("Basic Training");
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(60);

        var result = mockMvc.perform(post(BASE_PATH + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void testCreateTrainingWithFutureDate() throws Exception {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setTraineeUsername(TRAINEE_USERNAME);
        request.setTrainingName("Advanced Techniques");
        request.setTrainingDate(LocalDate.of(2025, 12, 31));
        request.setTrainingDuration(240);

        var result = mockMvc.perform(post(BASE_PATH + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void testGetTrainingTypesSuccess() throws Exception {
        List<TrainingTypeGetResponse> trainingTypes = JsonReaderUtil.readFromJson(
                "json/training-types-response.json",
                new TypeReference<>() {
                }
        );

        when(gymFacade.getAllTrainingTypes()).thenReturn(trainingTypes);

        var result = mockMvc.perform(get(BASE_PATH + "/trainings/types"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].trainingType").value(TRAINING_TYPE_1))
                .andExpect(jsonPath("$[1].trainingType").value(TRAINING_TYPE_2))
                .andExpect(jsonPath("$[2].trainingType").value(TRAINING_TYPE_3));

        verify(gymFacade).getAllTrainingTypes();
    }

    @Test
    void testGetTrainingTypesEmpty() throws Exception {
        when(gymFacade.getAllTrainingTypes()).thenReturn(List.of());

        var result = mockMvc.perform(get(BASE_PATH + "/trainings/types"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(gymFacade).getAllTrainingTypes();
    }

    @Test
    void testCreateTrainingInvalidJson() throws Exception {
        String invalidJson = "{ invalid json }";

        var result = mockMvc.perform(post(BASE_PATH + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTrainingMissingContentType() throws Exception {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setTraineeUsername(TRAINEE_USERNAME);
        request.setTrainingName(TRAINING_NAME);
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(TRAINING_DURATION);

        var result = mockMvc.perform(post(BASE_PATH + "/trainings")
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testCreateTrainingEmptyBody() throws Exception {
        var result = mockMvc.perform(post(BASE_PATH + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        result.andExpect(status().isOk());

        verify(gymFacade).createTraining(any(TrainingCreateRequest.class));
    }
}
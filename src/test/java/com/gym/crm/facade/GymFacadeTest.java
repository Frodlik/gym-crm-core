package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.model.TrainerModel;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainingCriteriaRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequest;
import com.gym.crm.dto.trainer.TrainerResponse;
import com.gym.crm.dto.trainer.TrainerTrainingCriteriaRequest;
import com.gym.crm.dto.trainer.TrainerUpdateRequest;
import com.gym.crm.dto.training.TrainingCreateRequest;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.openapi.model.AvailableTrainerGetResponse;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gym.crm.openapi.model.TraineeCreateRequest;
import com.gym.crm.openapi.model.TraineeCreateResponse;
import com.gym.crm.openapi.model.TraineeGetResponse;
import com.gym.crm.openapi.model.TraineeTrainingGetResponse;
import com.gym.crm.openapi.model.TraineeUpdateRequest;
import com.gym.crm.openapi.model.TraineeUpdateResponse;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.ADDRESS;
import static com.gym.crm.facade.GymTestObjects.BIRTH_DATE;
import static com.gym.crm.facade.GymTestObjects.FIRST_NAME;
import static com.gym.crm.facade.GymTestObjects.FITNESS_TYPE;
import static com.gym.crm.facade.GymTestObjects.LAST_NAME;
import static com.gym.crm.facade.GymTestObjects.PASSWORD;
import static com.gym.crm.facade.GymTestObjects.TRAINEE_ID;
import static com.gym.crm.facade.GymTestObjects.TRAINER_FIRST_NAME;
import static com.gym.crm.facade.GymTestObjects.TRAINER_USERNAME;
import static com.gym.crm.facade.GymTestObjects.TRAINING_ID;
import static com.gym.crm.facade.GymTestObjects.USERNAME;
import static com.gym.crm.facade.GymTestObjects.buildPasswordChangeRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerResponse;
import static com.gym.crm.facade.GymTestObjects.buildTrainerUpdateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingMapper trainingMapper;
    @InjectMocks
    private GymFacade facade;

    @Test
    void createTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        TraineeCreateRequestDto createRequestDto = new TraineeCreateRequestDto();
        TraineeCreateResponseDto serviceResponse = buildTraineeCreateResponseDto();
        TraineeCreateResponse expectedResponse = new TraineeCreateResponse();

        when(traineeMapper.toCreateRequest(request)).thenReturn(createRequestDto);
        when(traineeService.create(createRequestDto)).thenReturn(serviceResponse);
        when(traineeMapper.toRestCreateResponse(serviceResponse)).thenReturn(expectedResponse);

        TraineeCreateResponse actual = facade.createTrainee(request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(traineeMapper).toCreateRequest(request);
        verify(traineeService).create(createRequestDto);
        verify(traineeMapper).toRestCreateResponse(serviceResponse);
    }

    @Test
    void getTraineeByUsername_ShouldCallServiceAndReturnResponse() {
        TraineeGetResponseDto serviceResponse = buildTraineeGetResponseDto();
        TraineeGetResponse expectedResponse = new TraineeGetResponse();

        when(traineeService.findByUsername(USERNAME)).thenReturn(serviceResponse);
        when(traineeMapper.toRestGetResponse(serviceResponse)).thenReturn(expectedResponse);

        TraineeGetResponse actual = facade.getTraineeByUsername(USERNAME);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(traineeService).findByUsername(USERNAME);
        verify(traineeMapper).toRestGetResponse(serviceResponse);
    }

    @Test
    void updateTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        TraineeUpdateRequestDto requestDto = new TraineeUpdateRequestDto();
        TraineeUpdateResponseDto serviceResponse = buildTraineeUpdateResponseDto();
        TraineeUpdateResponse expectedResponse = new TraineeUpdateResponse();

        when(traineeMapper.toUpdateRequest(request)).thenReturn(requestDto);
        when(traineeService.update(requestDto, USERNAME)).thenReturn(serviceResponse);
        when(traineeMapper.toRestUpdateResponse(serviceResponse)).thenReturn(expectedResponse);

        TraineeUpdateResponse actual = facade.updateTrainee(USERNAME, request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(traineeMapper).toUpdateRequest(request);
        verify(traineeService).update(requestDto, USERNAME);
        verify(traineeMapper).toRestUpdateResponse(serviceResponse);
    }

    @Test
    void updateTraineeTrainersList_ShouldCallServiceAndReturnResponse() {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest();
        TraineeTrainersUpdateRequestDto requestDto = new TraineeTrainersUpdateRequestDto();
        TraineeTrainersUpdateResponseDto serviceResponse = buildTraineeTrainersUpdateResponseDto();
        TraineeAssignedTrainersUpdateResponse expectedResponse = new TraineeAssignedTrainersUpdateResponse();

        when(traineeMapper.toTrainersUpdateRequest(request)).thenReturn(requestDto);
        when(traineeService.updateTraineeTrainersList(requestDto, USERNAME)).thenReturn(serviceResponse);
        when(traineeMapper.toRestTrainersUpdateResponse(serviceResponse)).thenReturn(expectedResponse);

        TraineeAssignedTrainersUpdateResponse actual = facade.updateTraineeTrainersList(USERNAME, request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(traineeMapper).toTrainersUpdateRequest(request);
        verify(traineeService).updateTraineeTrainersList(requestDto, USERNAME);
        verify(traineeMapper).toRestTrainersUpdateResponse(serviceResponse);
    }

    @Test
    void deleteTrainee_ShouldCallService() {
        facade.deleteTrainee(USERNAME);

        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void changeTraineePassword_ShouldCallService() {
        PasswordChangeRequest request = buildPasswordChangeRequest();

        facade.changeTraineePassword(request);

        verify(traineeService).changePassword(request);
    }

    @Test
    void toggleTraineeActivation_ShouldCallService() {
        boolean isActive = false;

        facade.toggleTraineeActivation(USERNAME, isActive);

        verify(traineeService).toggleTraineeActivation(USERNAME, isActive);
    }

    @Test
    void createTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerCreateRequest request = buildTrainerCreateRequest();
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.create(request)).thenReturn(expectedResponse);

        TrainerResponse actual = facade.createTrainer(request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);

        verify(trainerService).create(request);
    }

    @Test
    void getTrainerByUsername_ShouldCallServiceAndReturnResponse() {
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(expectedResponse));

        Optional<TrainerResponse> actual = facade.getTrainerByUsername(TRAINER_USERNAME);

        assertThat(actual)
                .isPresent()
                .contains(expectedResponse);
        verify(trainerService).findByUsername(TRAINER_USERNAME);
    }

    @Test
    void getTrainerByUsername_ShouldReturnEmptyWhenNotFound() {
        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        Optional<TrainerResponse> actual = facade.getTrainerByUsername(TRAINER_USERNAME);

        assertThat(actual).isEmpty();
        verify(trainerService).findByUsername(TRAINER_USERNAME);
    }

    @Test
    void getTrainersNotAssignedToTrainee_ShouldCallServiceAndReturnMappedResponses() {
        TrainerResponse serviceResponse = buildTrainerResponse();
        List<TrainerResponse> serviceResponses = List.of(serviceResponse);
        AvailableTrainerGetResponse mappedResponse = new AvailableTrainerGetResponse();

        when(trainerService.findTrainersNotAssignedToTrainee(USERNAME)).thenReturn(serviceResponses);
        when(trainerMapper.toRestAvailableTrainerResponse(serviceResponse)).thenReturn(mappedResponse);

        List<AvailableTrainerGetResponse> actual = facade.getTrainersNotAssignedToTrainee(USERNAME);

        assertThat(actual)
                .hasSize(1)
                .containsExactly(mappedResponse);
        verify(trainerService).findTrainersNotAssignedToTrainee(USERNAME);
        verify(trainerMapper).toRestAvailableTrainerResponse(serviceResponse);
    }

    @Test
    void updateTrainer_ShouldCallServiceAndReturnResponse() {
        TrainerUpdateRequest request = buildTrainerUpdateRequest();
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.update(request)).thenReturn(expectedResponse);

        TrainerResponse actual = facade.updateTrainer(request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(trainerService).update(request);
    }

    @Test
    void changeTrainerPassword_ShouldCallService() {
        PasswordChangeRequest request = buildPasswordChangeRequest();

        facade.changeTrainerPassword(request);

        verify(trainerService).changePassword(request);
    }

    @Test
    void toggleTrainerActivation_ShouldCallServiceAndReturnResponse() {
        TrainerResponse expectedResponse = buildTrainerResponse();

        when(trainerService.toggleTrainerActivation(TRAINER_USERNAME)).thenReturn(expectedResponse);

        TrainerResponse actual = facade.toggleTrainerActivation(TRAINER_USERNAME);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(trainerService).toggleTrainerActivation(TRAINER_USERNAME);
    }

    @Test
    void createTraining_ShouldCallServiceAndReturnResponse() {
        TrainingCreateRequest request = buildTrainingCreateRequest();
        TrainingResponse expectedResponse = buildTrainingResponse();

        when(trainingService.create(request)).thenReturn(expectedResponse);

        TrainingResponse actual = facade.createTraining(request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(trainingService).create(request);
    }

    @Test
    void getTrainingById_ShouldCallServiceAndReturnResponse() {
        TrainingResponse expectedResponse = buildTrainingResponse();

        when(trainingService.findById(TRAINING_ID)).thenReturn(Optional.of(expectedResponse));

        Optional<TrainingResponse> actual = facade.getTrainingById(TRAINING_ID);

        assertThat(actual)
                .isPresent()
                .contains(expectedResponse);
        verify(trainingService).findById(TRAINING_ID);
    }

    @Test
    void getTrainingById_ShouldReturnEmptyWhenNotFound() {
        Long nonExistentId = 999L;

        when(trainingService.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<TrainingResponse> actual = facade.getTrainingById(nonExistentId);

        assertThat(actual).isEmpty();
        verify(trainingService).findById(nonExistentId);
    }

    @Test
    void getTraineeTrainingsByCriteria_ShouldCallServiceAndReturnResponse() {
        TraineeTrainingCriteriaRequestDto request = buildTraineeTrainingCriteriaRequestDto();
        TrainingResponse serviceResponse = buildTrainingResponse();
        TraineeTrainingGetResponse mappedResponse = new TraineeTrainingGetResponse();
        List<TrainingResponse> serviceResponses = List.of(serviceResponse);

        when(trainingService.getTraineeTrainingsByCriteria(
                request.getTraineeUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(),
                request.getTrainingType()
        )).thenReturn(serviceResponses);
        when(trainingMapper.toRestTrainingGetResponse(serviceResponse)).thenReturn(mappedResponse);

        List<TraineeTrainingGetResponse> actual = facade.getTraineeTrainingsByCriteria(request);

        assertThat(actual)
                .hasSize(1)
                .containsExactly(mappedResponse);
        verify(trainingService).getTraineeTrainingsByCriteria(
                request.getTraineeUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(),
                request.getTrainingType()
        );
        verify(trainingMapper).toRestTrainingGetResponse(serviceResponse);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldCallServiceAndReturnResponse() {
        TrainerTrainingCriteriaRequest request = buildTrainerTrainingCriteriaRequestDto();
        TrainingResponse expectedResponse = buildTrainingResponse();
        List<TrainingResponse> expectedResponses = List.of(expectedResponse);

        when(trainingService.getTrainerTrainingsByCriteria(
                request.getTrainerUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName()
        )).thenReturn(expectedResponses);

        List<TrainingResponse> actual = facade.getTrainerTrainingsByCriteria(request);

        assertThat(actual)
                .hasSize(1)
                .containsExactly(expectedResponse);
        verify(trainingService).getTrainerTrainingsByCriteria(
                request.getTrainerUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName()
        );
    }

    private TraineeGetResponseDto buildTraineeGetResponseDto() {
        TraineeGetResponseDto response = new TraineeGetResponseDto();
        response.setId(TRAINEE_ID);
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setUsername(USERNAME);
        response.setActive(true);
        response.setDateOfBirth(BIRTH_DATE);
        response.setAddress(ADDRESS);

        return response;
    }

    private TraineeTrainingCriteriaRequestDto buildTraineeTrainingCriteriaRequestDto() {
        TraineeTrainingCriteriaRequestDto request = new TraineeTrainingCriteriaRequestDto();
        request.setTraineeUsername(USERNAME);
        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 12, 31));
        request.setTrainerName(TRAINER_FIRST_NAME);
        request.setTrainingType(FITNESS_TYPE);

        return request;
    }

    private TrainerTrainingCriteriaRequest buildTrainerTrainingCriteriaRequestDto() {
        TrainerTrainingCriteriaRequest request = new TrainerTrainingCriteriaRequest();
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 12, 31));
        request.setTraineeName(FIRST_NAME);

        return request;
    }

    private TraineeCreateResponseDto buildTraineeCreateResponseDto() {
        TraineeCreateResponseDto response = new TraineeCreateResponseDto();
        response.setUsername(USERNAME);
        response.setPassword(PASSWORD);

        return response;
    }

    private TraineeUpdateResponseDto buildTraineeUpdateResponseDto() {
        TraineeUpdateResponseDto response = new TraineeUpdateResponseDto();
        response.setFirstName(FIRST_NAME);
        response.setLastName(LAST_NAME);
        response.setUsername(USERNAME);
        response.setActive(true);
        response.setDateOfBirth(BIRTH_DATE);
        response.setAddress(ADDRESS);

        return response;
    }

    private TraineeTrainersUpdateResponseDto buildTraineeTrainersUpdateResponseDto() {
        TrainerModel trainer = TrainerModel.builder()
                .username("trainer.john")
                .firstName("John")
                .lastName("Doe")
                .build();

        TraineeTrainersUpdateResponseDto response = new TraineeTrainersUpdateResponseDto();
        response.setTrainers(List.of(trainer));

        return response;
    }
}
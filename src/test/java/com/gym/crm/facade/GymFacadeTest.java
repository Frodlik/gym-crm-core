package com.gym.crm.facade;

import com.gym.crm.dto.PasswordChangeRequest;
import com.gym.crm.dto.trainee.TraineeCreateRequestDto;
import com.gym.crm.dto.trainee.TraineeCreateResponseDto;
import com.gym.crm.dto.trainee.TraineeGetResponseDto;
import com.gym.crm.dto.trainee.TraineeSearchFilter;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainersUpdateResponseDto;
import com.gym.crm.dto.trainee.TraineeTrainingCriteriaRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateRequestDto;
import com.gym.crm.dto.trainee.TraineeUpdateResponseDto;
import com.gym.crm.dto.trainer.AvailableTrainerResponseDto;
import com.gym.crm.dto.trainer.TrainerCreateRequestDto;
import com.gym.crm.dto.trainer.TrainerCreateResponseDto;
import com.gym.crm.dto.trainer.TrainerGetResponseDto;
import com.gym.crm.dto.trainer.TrainerSearchFilter;
import com.gym.crm.dto.trainer.TrainerTrainingCriteriaRequest;
import com.gym.crm.dto.trainer.TrainerUpdateRequestDto;
import com.gym.crm.dto.trainer.TrainerUpdateResponseDto;
import com.gym.crm.dto.training.TrainingCreateRequestDto;
import com.gym.crm.dto.training.TrainingResponse;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.openapi.model.AvailableTrainerGetResponse;
import com.gym.crm.openapi.model.ChangePasswordRequest;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gym.crm.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gym.crm.openapi.model.TraineeCreateRequest;
import com.gym.crm.openapi.model.TraineeCreateResponse;
import com.gym.crm.openapi.model.TraineeGetResponse;
import com.gym.crm.openapi.model.TraineeTrainingGetResponse;
import com.gym.crm.openapi.model.TraineeUpdateRequest;
import com.gym.crm.openapi.model.TraineeUpdateResponse;
import com.gym.crm.openapi.model.TrainerCreateRequest;
import com.gym.crm.openapi.model.TrainerCreateResponse;
import com.gym.crm.openapi.model.TrainerGetResponse;
import com.gym.crm.openapi.model.TrainerTrainingGetResponse;
import com.gym.crm.openapi.model.TrainerUpdateRequest;
import com.gym.crm.openapi.model.TrainerUpdateResponse;
import com.gym.crm.openapi.model.TrainingCreateRequest;
import com.gym.crm.security.AuthenticationContext;
import com.gym.crm.service.AuthenticationService;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.gym.crm.facade.GymTestObjects.TRAINER_USERNAME;
import static com.gym.crm.facade.GymTestObjects.TRAINING_ID;
import static com.gym.crm.facade.GymTestObjects.USERNAME;
import static com.gym.crm.facade.GymTestObjects.buildAvailableTrainerResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildPasswordChangeRequest;
import static com.gym.crm.facade.GymTestObjects.buildTraineeCreateRequestDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeCreateResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeGetResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeTrainersUpdateRequestDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeTrainersUpdateResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeTrainingCriteriaRequestDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeUpdateRequestDto;
import static com.gym.crm.facade.GymTestObjects.buildTraineeUpdateResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildTrainerCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerCreateResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildTrainerResponse;
import static com.gym.crm.facade.GymTestObjects.buildTrainerTrainingCriteriaRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerUpdateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainerUpdateResponseDto;
import static com.gym.crm.facade.GymTestObjects.buildTrainingCreateRequest;
import static com.gym.crm.facade.GymTestObjects.buildTrainingResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private HttpServletResponse servletResponse;
    @InjectMocks
    private GymFacade facade;

    @Test
    void createTrainee_ShouldCallServiceAndReturnResponse() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        TraineeCreateRequestDto createRequestDto = buildTraineeCreateRequestDto();
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
        TraineeUpdateRequestDto requestDto = buildTraineeUpdateRequestDto();
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
        TraineeTrainersUpdateRequestDto requestDto = buildTraineeTrainersUpdateRequestDto();
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
    void changePassword_ShouldCallService() {
        PasswordChangeRequest request = buildPasswordChangeRequest();
        ChangePasswordRequest facadeRequest = new ChangePasswordRequest(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        when(authenticationContext.getCurrentUserType()).thenReturn(Optional.of("TRAINEE"));

        facade.changePassword(facadeRequest);

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
        TrainerCreateRequest request = new TrainerCreateRequest();
        TrainerCreateRequestDto requestDto = buildTrainerCreateRequest();
        TrainerCreateResponseDto serviceResponse = buildTrainerCreateResponseDto();
        TrainerCreateResponse expectedResponse = new TrainerCreateResponse();

        when(trainerMapper.toCreateRequestDto(request)).thenReturn(requestDto);
        when(trainerService.create(requestDto)).thenReturn(serviceResponse);
        when(trainerMapper.toRestCreateResponse(serviceResponse)).thenReturn(expectedResponse);

        TrainerCreateResponse actual = facade.createTrainer(request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(trainerMapper).toCreateRequestDto(request);
        verify(trainerService).create(requestDto);
        verify(trainerMapper).toRestCreateResponse(serviceResponse);
    }

    @Test
    void getTrainerByUsername_ShouldCallServiceAndReturnResponse() {
        TrainerGetResponseDto serviceResponse = buildTrainerResponse();
        TrainerGetResponse expectedResponse = new TrainerGetResponse();

        when(trainerService.findByUsername(TRAINER_USERNAME)).thenReturn(serviceResponse);
        when(trainerMapper.toRestGetResponse(serviceResponse)).thenReturn(expectedResponse);

        TrainerGetResponse actual = facade.getTrainerByUsername(TRAINER_USERNAME);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(trainerService).findByUsername(TRAINER_USERNAME);
        verify(trainerMapper).toRestGetResponse(serviceResponse);
    }

    @Test
    void getTrainersNotAssignedToTrainee_ShouldCallServiceAndReturnMappedResponses() {
        AvailableTrainerResponseDto serviceResponse = buildAvailableTrainerResponseDto();
        List<AvailableTrainerResponseDto> serviceResponses = List.of(serviceResponse);
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
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        TrainerUpdateRequestDto requestDto = buildTrainerUpdateRequest();
        TrainerUpdateResponseDto serviceResponse = buildTrainerUpdateResponseDto();
        TrainerUpdateResponse expectedResponse = new TrainerUpdateResponse();

        when(trainerMapper.toUpdateRequestDto(request)).thenReturn(requestDto);
        when(trainerService.update(requestDto, TRAINER_USERNAME)).thenReturn(serviceResponse);
        when(trainerMapper.toRestUpdateResponse(serviceResponse)).thenReturn(expectedResponse);

        TrainerUpdateResponse actual = facade.updateTrainer(TRAINER_USERNAME, request);

        assertThat(actual)
                .isNotNull()
                .isEqualTo(expectedResponse);
        verify(trainerMapper).toUpdateRequestDto(request);
        verify(trainerService).update(requestDto, TRAINER_USERNAME);
        verify(trainerMapper).toRestUpdateResponse(serviceResponse);
    }

    @Test
    void toggleTrainerActivation_ShouldCallService() {
        boolean isActive = false;

        facade.toggleTrainerActivation(TRAINER_USERNAME, isActive);

        verify(trainerService).toggleTrainerActivation(TRAINER_USERNAME, isActive);
    }

    @Test
    void createTraining_ShouldCallService() {
        TrainingCreateRequest restRequest = new TrainingCreateRequest();
        TrainingCreateRequestDto requestDto = buildTrainingCreateRequest();

        when(trainingMapper.toCreateRequestDto(restRequest)).thenReturn(requestDto);

        doNothing().when(trainingService).create(requestDto);

        facade.createTraining(restRequest);

        verify(trainingMapper).toCreateRequestDto(restRequest);
        verify(trainingService).create(requestDto);
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

        TraineeSearchFilter expectedFilter = TraineeSearchFilter.builder()
                .traineeUsername(request.getTraineeUsername())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .trainerName(request.getTrainerName())
                .trainingType(request.getTrainingType())
                .build();

        when(trainingService.getTraineeTrainingsByCriteria(expectedFilter)).thenReturn(serviceResponses);
        when(trainingMapper.toRestTraineeTrainingGetResponse(serviceResponse)).thenReturn(mappedResponse);

        List<TraineeTrainingGetResponse> actual = facade.getTraineeTrainingsByCriteria(request);

        assertThat(actual)
                .hasSize(1)
                .containsExactly(mappedResponse);
        verify(trainingService).getTraineeTrainingsByCriteria(expectedFilter);
        verify(trainingMapper).toRestTraineeTrainingGetResponse(serviceResponse);
    }

    @Test
    void getTrainerTrainingsByCriteria_ShouldCallServiceAndReturnResponse() {
        TrainerTrainingCriteriaRequest request = buildTrainerTrainingCriteriaRequest();
        TrainingResponse serviceResponse = buildTrainingResponse();
        TrainerTrainingGetResponse mappedResponse = new TrainerTrainingGetResponse();
        List<TrainingResponse> serviceResponses = List.of(serviceResponse);

        TrainerSearchFilter expectedFilter = TrainerSearchFilter.builder()
                .trainerUsername(request.getTrainerUsername())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .traineeName(request.getTraineeName())
                .build();

        when(trainingService.getTrainerTrainingsByCriteria(expectedFilter)).thenReturn(serviceResponses);
        when(trainingMapper.toRestTrainerTrainingGetResponse(serviceResponse)).thenReturn(mappedResponse);

        List<TrainerTrainingGetResponse> actual = facade.getTrainerTrainingsByCriteria(request);

        assertThat(actual)
                .hasSize(1)
                .containsExactly(mappedResponse);
        verify(trainingService).getTrainerTrainingsByCriteria(expectedFilter);
        verify(trainingMapper).toRestTrainerTrainingGetResponse(serviceResponse);
    }

    @Test
    void logout_whenSuccessful_shouldCallAuthenticationService() {
        doNothing().when(authenticationService).logout(servletRequest, servletResponse);

        facade.logout(servletRequest, servletResponse);

        verify(authenticationService).logout(servletRequest, servletResponse);
    }

    @Test
    void testLogoutThrowsException() {
        doThrow(new RuntimeException("Logout failed"))
                .when(authenticationService).logout(servletRequest, servletResponse);

        assertThrows(RuntimeException.class, () -> facade.logout(servletRequest, servletResponse));
        verify(authenticationService).logout(servletRequest, servletResponse);
    }
}
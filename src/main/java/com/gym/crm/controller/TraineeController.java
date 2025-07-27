package com.gym.crm.controller;

import com.gym.crm.dto.trainee.TraineeTrainingCriteriaRequestDto;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static com.gym.crm.controller.ApiConstant.BASE_PATH;

@RestController
@RequestMapping(BASE_PATH + "/trainees")
@RequiredArgsConstructor
public class TraineeController {
    private final GymFacade gymFacade;

    @PostMapping("/register")
    public ResponseEntity<TraineeCreateResponse> registerTrainee(@Valid @RequestBody TraineeCreateRequest request) {
        TraineeCreateResponse response = gymFacade.createTrainee(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeGetResponse> getTraineeProfile(@PathVariable("username") String username) {
        TraineeGetResponse response = gymFacade.getTraineeByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(@PathVariable("username") String username, @Valid @RequestBody TraineeUpdateRequest request) {
        TraineeUpdateResponse response = gymFacade.updateTrainee(username, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable("username") String username) {
        gymFacade.deleteTrainee(username);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/change-activation-status")
    public ResponseEntity<Void> changeActivationStatus(@PathVariable("username") String username, @Valid @RequestBody ActivationStatusRequest request) {
        gymFacade.toggleTraineeActivation(username, request.getIsActive());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/available-trainers")
    public ResponseEntity<List<AvailableTrainerGetResponse>> getAvailableTrainers(@PathVariable("username") String username) {
        List<AvailableTrainerGetResponse> response = gymFacade.getTrainersNotAssignedToTrainee(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeAssignedTrainersUpdateResponse> updateTraineeTrainers(@PathVariable("username") String username, @Valid @RequestBody TraineeAssignedTrainersUpdateRequest request) {
        TraineeAssignedTrainersUpdateResponse response = gymFacade.updateTraineeTrainersList(username, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingGetResponse>> getTraineeTrainings(
            @PathVariable("username") String username,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "trainerName", required = false) String trainerName) {

        TraineeTrainingCriteriaRequestDto request = TraineeTrainingCriteriaRequestDto.builder()
                .traineeUsername(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .trainerName(trainerName)
                .build();

        List<TraineeTrainingGetResponse> response = gymFacade.getTraineeTrainingsByCriteria(request);

        return ResponseEntity.ok(response);
    }
}

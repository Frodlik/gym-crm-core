package com.gym.crm.controller;

import com.gym.crm.dto.trainer.TrainerTrainingCriteriaRequest;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ActivationStatusRequest;
import com.gym.crm.openapi.model.TrainerCreateRequest;
import com.gym.crm.openapi.model.TrainerCreateResponse;
import com.gym.crm.openapi.model.TrainerGetResponse;
import com.gym.crm.openapi.model.TrainerTrainingGetResponse;
import com.gym.crm.openapi.model.TrainerUpdateRequest;
import com.gym.crm.openapi.model.TrainerUpdateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(BASE_PATH + "/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final GymFacade gymFacade;

    @PostMapping("/register")
    public ResponseEntity<TrainerCreateResponse> registerTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        TrainerCreateResponse response = gymFacade.createTrainer(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerGetResponse> getTrainerProfile(@PathVariable("username") String username) {
        TrainerGetResponse response = gymFacade.getTrainerByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(@PathVariable("username") String username, @Valid @RequestBody TrainerUpdateRequest request) {
        TrainerUpdateResponse response = gymFacade.updateTrainer(username, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/change-activation-status")
    public ResponseEntity<Void> changeActivationStatus(@PathVariable("username") String username, @Valid @RequestBody ActivationStatusRequest request) {
        gymFacade.toggleTrainerActivation(username, request.getIsActive());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingGetResponse>> getTrainerTrainings(
            @PathVariable("username") String username,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "traineeName", required = false) String traineeName) {

        TrainerTrainingCriteriaRequest criteria = TrainerTrainingCriteriaRequest.builder()
                .trainerUsername(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .traineeName(traineeName)
                .build();

        List<TrainerTrainingGetResponse> response = gymFacade.getTrainerTrainingsByCriteria(criteria);

        return ResponseEntity.ok(response);
    }
}

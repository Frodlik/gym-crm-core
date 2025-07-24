package com.gym.crm.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.TrainingCreateRequest;
import com.gym.crm.openapi.model.TrainingTypeGetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.gym.crm.controller.ApiConstant.BASE_PATH;

@RestController
@RequestMapping(BASE_PATH + "/trainings")
@RequiredArgsConstructor
public class TrainingController {
    private final GymFacade gymFacade;

    @PostMapping
    public ResponseEntity<Void> createTraining(@RequestBody TrainingCreateRequest request) {
        gymFacade.createTraining(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeGetResponse>> getTrainingTypes() {
        List<TrainingTypeGetResponse> responses = gymFacade.getAllTrainingTypes();

        return ResponseEntity.ok().body(responses);
    }
}

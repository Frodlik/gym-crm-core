package com.gym.crm.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ErrorResponse;
import com.gym.crm.openapi.model.TrainingCreateRequest;
import com.gym.crm.openapi.model.TrainingTypeGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Training", description = "Endpoints for managing training sessions and types")
public class TrainingController {
    private final GymFacade gymFacade;

    @Operation(summary = "Add new training", description = "Creates a new training session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(description = "Unexpected error")
    })
    @PostMapping
    public ResponseEntity<Void> createTraining(@RequestBody TrainingCreateRequest request) {
        gymFacade.createTraining(request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get training types", description = "Retrieves all available training types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully", content = @Content(schema = @Schema(implementation = TrainingTypeGetResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(description = "Unexpected error")
    })
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeGetResponse>> getTrainingTypes() {
        List<TrainingTypeGetResponse> responses = gymFacade.getAllTrainingTypes();

        return ResponseEntity.ok().body(responses);
    }
}

package com.gym.crm.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ChangePasswordRequest;
import com.gym.crm.openapi.model.ErrorResponse;
import com.gym.crm.openapi.model.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.gym.crm.controller.ApiConstant.BASE_PATH;

@RestController
@RequestMapping(BASE_PATH + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and password management")
public class AuthController {
    private final GymFacade gymFacade;

    @Operation(summary = "User login", description = "Authenticates user with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(description = "Unexpected error")
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        gymFacade.login(request, response);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change user password", description = "Changes user password with old password verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid old password", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(description = "Unexpected error")
    })
    @PutMapping("/login")
    public ResponseEntity<Void> changeLogin(@RequestBody ChangePasswordRequest request) {
        gymFacade.changePassword(request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "User logout", description = "Logs out the current user by clearing authentication tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(description = "Unexpected error")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        gymFacade.logout(request, response);

        return ResponseEntity.ok().build();
    }
}

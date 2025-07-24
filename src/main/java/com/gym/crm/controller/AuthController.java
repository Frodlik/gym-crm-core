package com.gym.crm.controller;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ChangePasswordRequest;
import com.gym.crm.openapi.model.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
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
public class AuthController {
    private final GymFacade gymFacade;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        gymFacade.login(request, httpRequest);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/login")
    public ResponseEntity<Void> changeLogin(@RequestBody ChangePasswordRequest request) {
        gymFacade.changePassword(request);

        return ResponseEntity.ok().build();
    }
}

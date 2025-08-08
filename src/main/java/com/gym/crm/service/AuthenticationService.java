package com.gym.crm.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    void authenticateAndSetToken(String username, String password, HttpServletResponse response);

    void refreshAccessToken(HttpServletRequest request, HttpServletResponse response);

    String validateCredentials(String username, String password);

    void validateTraineeCredentials(String username, String password);

    void validateTrainerCredentials(String username, String password);
}

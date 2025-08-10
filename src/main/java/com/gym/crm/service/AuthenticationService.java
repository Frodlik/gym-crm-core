package com.gym.crm.service;

import com.gym.crm.service.enums.UserType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    void authenticateAndSetToken(String username, String password, HttpServletResponse response);

    void refreshAccessToken(HttpServletRequest request, HttpServletResponse response);

    UserType validateCredentials(String username, String password);

    void validateTraineeCredentials(String username, String password);

    void validateTrainerCredentials(String username, String password);

    void logout(HttpServletRequest request, HttpServletResponse response);
}

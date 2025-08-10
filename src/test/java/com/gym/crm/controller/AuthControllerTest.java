package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.openapi.model.ChangePasswordRequest;
import com.gym.crm.openapi.model.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.gym.crm.controller.ApiConstant.BASE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    private static final String USERNAME = "naruto.uzumaki";
    private static final String PASSWORD = "hokage123";
    private static final String NEW_PASSWORD = "hokage456";
    private static final String OLD_PASSWORD = "oldPassword123";

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @Mock
    private GymFacade gymFacade;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        doNothing().when(gymFacade).login(any(LoginRequest.class), any(HttpServletResponse.class));

        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(gymFacade).login(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void testLoginWithEmptyUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword(PASSWORD);

        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).login(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void testLoginWithNullUsername() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword(PASSWORD);

        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).login(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void testLoginWithEmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword("");

        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).login(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void testLoginWithNullPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(null);

        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).login(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void testLoginWithMalformedJson() throws Exception {
        String malformedJson = "{ \"username\": \"test\", \"password\": }";

        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void testLoginWithoutBody() throws Exception {
        var result = mockMvc.perform(post(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(OLD_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        doNothing().when(gymFacade).changePassword(any(ChangePasswordRequest.class));

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testChangePasswordWithEmptyUsername() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("");
        request.setOldPassword(OLD_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testChangePasswordWithNullUsername() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(null);
        request.setOldPassword(OLD_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testChangePasswordWithEmptyOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword("");
        request.setNewPassword(NEW_PASSWORD);

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testChangePasswordWithEmptyNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(OLD_PASSWORD);
        request.setNewPassword("");

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testChangePasswordWithSameOldAndNewPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword(PASSWORD);

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testChangePasswordWithMalformedJson() throws Exception {
        String malformedJson = "{ \"username\": \"test\", \"oldPassword\": \"old\", \"newPassword\": }";

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordWithoutBody() throws Exception {
        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void testChangePasswordWithNullValues() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(null);
        request.setOldPassword(null);
        request.setNewPassword(null);

        var result = mockMvc.perform(put(BASE_PATH + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        doNothing().when(gymFacade).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        var result = mockMvc.perform(post(BASE_PATH + "/auth/logout")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(gymFacade).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void logout_withoutContentType_shouldStillWork() throws Exception {
        doNothing().when(gymFacade).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post(BASE_PATH + "/auth/logout"))
                .andExpect(status().isOk());

        verify(gymFacade).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void logout_withGetMethod_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/auth/logout").with(request -> {
                    request.setMethod("GET");
                    return request;
                }))
                .andExpect(status().isMethodNotAllowed());
    }
}

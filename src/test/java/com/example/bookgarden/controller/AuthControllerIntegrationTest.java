package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.service.AuthService;
import com.example.bookgarden.service.OTPService;
import com.example.bookgarden.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OTPService otpService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testSendForgotPasswordOtp() throws Exception {
        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setEmail("test@example.com");

        doNothing().when(otpService).sendForgotPasswordOtp(any());

        mockMvc.perform(post("/api/v1/auth/send-forgot-password-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"OTP sent successfully!\",\"data\":null}"));
    }

    @Test
    void testRegisterUser() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFullName("Tester 01");
        registerDTO.setEmail("test01@example.com");
        registerDTO.setPhone("0123456789");
        registerDTO.setPassWord("password123");
        registerDTO.setConfirmPassWord("password123");

        when(authService.registerUser(any())).thenReturn(GenericResponse.builder()
                .success(true)
                .message("Đăng ký thành công!")
                .data(null)
                .build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"Đăng ký thành công!\",\"data\":null}"));
    }

    @Test
    void testVerifyOtp() throws Exception {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
        verifyOtpRequest.setEmail("test@example.com");
        verifyOtpRequest.setOtp("123456");

        when(otpService.verifyOtp(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/verify-OTP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"OTP verified successfully!\",\"data\":null}"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

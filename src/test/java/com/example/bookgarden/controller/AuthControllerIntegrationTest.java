package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.AuthService;
import com.example.bookgarden.service.OTPService;
import com.example.bookgarden.service.TokenService;
import com.example.bookgarden.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

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

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setEmail("test@example.com");
        forgotPasswordDTO.setPassWord("newPassword123");
        forgotPasswordDTO.setConfirmPassWord("newPassword123");

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Password reset successfully!")
                .data(null)
                .build();

        when(authService.forgotPassword(any(ForgotPasswordDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(forgotPasswordDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"Password reset successfully!\",\"data\":null}"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password123");
        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Login successfully!")
                .data(new HashMap<String, String>() {{
                    put("accessToken", "accessToken");
                    put("refreshToken", "refreshToken");
                }})
                .build();

        when(authService.login(any(LoginDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"passWord\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"Login successfully!\",\"data\":{\"accessToken\":\"accessToken\",\"refreshToken\":\"refreshToken\"}}"));
    }

    @Test
    void testLogin_InvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"passWord\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Tài khoản không tồn tại")
                .data(null)
                .build();

        when(authService.login(any(LoginDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notfound@example.com\",\"passWord\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"message\":\"Tài khoản không tồn tại\",\"data\":null}"));
    }
    @Test
    void testLogout_Success() throws Exception {
        TokenRequestDTO tokenRequestDTO = new TokenRequestDTO();
        tokenRequestDTO.setRefreshToken("refreshToken");

        ResponseEntity<GenericResponse> response = ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Logout successfully!")
                .data(null)
                .build());

        when(authService.logout(anyString(), any(TokenRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tokenRequestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"Logout successfully!\",\"data\":null}"));
    }

    @Test
    void testLogout_Failed() throws Exception {
        TokenRequestDTO tokenRequestDTO = new TokenRequestDTO();
        tokenRequestDTO.setRefreshToken("refreshToken");

        ResponseEntity<GenericResponse> response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Logout failed!")
                        .data("Please login before logout!")
                        .build());

        when(authService.logout(anyString(), any(TokenRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tokenRequestDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"success\":false,\"message\":\"Logout failed!\",\"data\":\"Please login before logout!\"}"));
    }
    @Test
    void testLogoutAll_Success() throws Exception {
        ResponseEntity<GenericResponse> response = ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Logged out from all devices successfully!")
                .data(null)
                .build());

        when(authService.logoutAll(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"Logged out from all devices successfully!\",\"data\":null}"));
    }

    @Test
    void testLogoutAll_Failed() throws Exception {
        ResponseEntity<GenericResponse> response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Logout failed!")
                        .data("Invalid access token!")
                        .build());

        when(authService.logoutAll(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"success\":false,\"message\":\"Logout failed!\",\"data\":\"Invalid access token!\"}"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

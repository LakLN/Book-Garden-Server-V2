package com.example.bookgarden.controller;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UpdateProfileRequestDTO;
import com.example.bookgarden.dto.UserDTO;
import com.example.bookgarden.entity.Address;
import com.example.bookgarden.security.JwtTokenProvider;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetProfile_Success() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("userId");
        userDTO.setFullName("Tester 01");
        userDTO.setEmail("test01@example.com");
        userDTO.setAddresses(Collections.singletonList(new Address("addressId1", "123 Main St")));

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Successfully retrieved user profile")
                .data(userDTO)
                .build();

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("userId");
        when(userService.getProfile(anyString())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer mockToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    void testGetProfile_UserNotFound() throws Exception {
        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("User not found")
                .data(null)
                .build();

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("invalidUserId");
        when(userService.getProfile(anyString())).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));

        mockMvc.perform(get("/api/v1/user/profile")
                        .header("Authorization", "Bearer mockToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    void testUpdateProfile_Success() throws Exception {
        String token = "Bearer mockToken";
        String userId = "userId1";

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn(userId);

        UpdateProfileRequestDTO updateProfileRequestDTO = new UpdateProfileRequestDTO();
        updateProfileRequestDTO.setFullName("New Name");

        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", "some-image".getBytes());

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Successfully updated user profile")
                .data(null)
                .build();

        when(userService.updateProfile(anyString(), any(UpdateProfileRequestDTO.class), any(MultipartHttpServletRequest.class))).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(multipart("/api/v1/user/profile/updateProfile")
                        .file(avatarFile)
                        .header("Authorization", token)
                        .param("fullName", "New Name")
                        .param("email", "test@example.com")
                        .param("phone", "0123456789")
                        .param("gender", "Male")
                        .param("birthday", "2000-01-01")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true,\"message\":\"Successfully updated user profile\",\"data\":null}"));
    }
}

package com.example.bookgarden.controller;

import com.example.bookgarden.dto.AuthorResponseDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UpdateAuthorRequestDTO;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.AuthorService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAuthors_Success() throws Exception {
        List<AuthorResponseDTO> authorDTOs = new ArrayList<>();
        // Add mock data to authorDTOs list

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy danh sách tác giả thành công")
                .data(authorDTOs)
                .build();

        when(authorService.getAllAuthors()).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Lấy danh sách tác giả thành công")));
    }

    @Test
    void testGetAllAuthors_Error() throws Exception {
        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Lỗi lấy danh sách tác giả")
                .data(null)
                .build();

        when(authorService.getAllAuthors()).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));

        mockMvc.perform(get("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Lỗi lấy danh sách tác giả")));
    }
    @Test
    void testUpdateAuthor_Success() throws Exception {
        String authorId = "60d72b2f9b1e8b3a6c8e7c1d";
        UpdateAuthorRequestDTO updateAuthorRequestDTO = new UpdateAuthorRequestDTO();
        updateAuthorRequestDTO.setAuthorName("Updated Author Name");

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Cập nhật tác giả thành công")
                .data(null)
                .build();

        when(jwtTokenProvider.getUserIdFromJwt("mock-token")).thenReturn("user-id");
        when(authorService.updateAuthor("user-id", authorId, updateAuthorRequestDTO)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(put("/api/v1/authors/{authorId}", authorId)
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorName\":\"Updated Author Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Cập nhật tác giả thành công")));
    }
    @Test
    void testGetAuthorById_Success() throws Exception {
        String authorId = "60d72b2f9b1e8b3a6c8e7c1d";
        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy thông tin tác giả thành công")
                .data(null)
                .build();

        when(authorService.getAuthorById(authorId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/authors/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Lấy thông tin tác giả thành công")));
    }

    @Test
    void testGetAuthorById_NotFound() throws Exception {
        String authorId = "60d72b2f9b1e8b3a6c8e7c1d";
        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Không tìm thấy tác giả")
                .data(null)
                .build();

        when(authorService.getAuthorById(authorId)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));

        mockMvc.perform(get("/api/v1/authors/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Không tìm thấy tác giả")));
    }
    @Test
    void testAddAuthor_ExistingAuthor() throws Exception {
        String token = "Bearer mock-token";
        UpdateAuthorRequestDTO addAuthorRequestDTO = new UpdateAuthorRequestDTO();
        addAuthorRequestDTO.setAuthorName("Existing Author");

        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Tác giả Existing Author đã tồn tại")
                .data(null)
                .build();

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("mockUserId");
        when(authorService.addAuthor(anyString(), any(UpdateAuthorRequestDTO.class))).thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).body(response));

        mockMvc.perform(post("/api/v1/authors/add")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(addAuthorRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Tác giả Existing Author đã tồn tại")));
    }
}

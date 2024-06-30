package com.example.bookgarden.controller;

import com.example.bookgarden.dto.AddCategoryRequestDTO;
import com.example.bookgarden.dto.BookDTO;
import com.example.bookgarden.dto.CategoryResponseDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void testGetAllCategories_Success() throws Exception {
        List<BookDTO> bookDTOs = Arrays.asList(
                new BookDTO("1", "Book 1"),
                new BookDTO("2", "Book 2")
        );

        List<CategoryResponseDTO> categories = Arrays.asList(
                new CategoryResponseDTO("1", "Fiction", bookDTOs),
                new CategoryResponseDTO("2", "Non-Fiction", Collections.emptyList())
        );

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy danh sách danh mục thành công")
                .data(categories)
                .build();

        when(categoryService.getAllCategories()).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Lấy danh sách danh mục thành công")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].categoryName", is("Fiction")))
                .andExpect(jsonPath("$.data[0].books", hasSize(2)))
                .andExpect(jsonPath("$.data[0].books[0].title", is("Book 1")));
    }
    @Test
    void testGetCategoryById_Success() throws Exception {
        String categoryId = new ObjectId().toString();
        List<BookDTO> bookDTOs = Arrays.asList(
                new BookDTO("1", "Book 1"),
                new BookDTO("2", "Book 2")
        );

        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO(categoryId, "Fiction", bookDTOs);

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy thông tin danh mục thành công")
                .data(categoryResponseDTO)
                .build();

        when(categoryService.getCategoryById(anyString())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Lấy thông tin danh mục thành công")))
                .andExpect(jsonPath("$.data.id", is(categoryId)))
                .andExpect(jsonPath("$.data.categoryName", is("Fiction")))
                .andExpect(jsonPath("$.data.books", hasSize(2)))
                .andExpect(jsonPath("$.data.books[0].title", is("Book 1")));
    }
    @Test
    void testGetCategoryById_NotFound() throws Exception {
        String categoryId = new ObjectId().toString();
        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Không tìm thấy danh mục")
                .data(null)
                .build();

        when(categoryService.getCategoryById(anyString())).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Không tìm thấy danh mục")));
    }
    @Test
    void testAddCategory_Success() throws Exception {
        AddCategoryRequestDTO addCategoryRequestDTO = new AddCategoryRequestDTO();
        addCategoryRequestDTO.setCategoryName("New Category");

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Thêm danh mục thành công")
                .data(null)
                .build();

        when(categoryService.addCategory(anyString(), any(AddCategoryRequestDTO.class))).thenReturn(ResponseEntity.ok(response));
        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("userId");
        when(categoryService.checkAdminPermission(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/categories/add")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(addCategoryRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Thêm danh mục thành công")));
    }

    @Test
    void testAddCategory_ExistingCategory() throws Exception {
        AddCategoryRequestDTO addCategoryRequestDTO = new AddCategoryRequestDTO();
        addCategoryRequestDTO.setCategoryName("Existing Category");

        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Danh mục đã tồn tại")
                .data(null)
                .build();

        when(categoryService.addCategory(anyString(), any(AddCategoryRequestDTO.class))).thenReturn(ResponseEntity.status(409).body(response));
        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("userId");
        when(categoryService.checkAdminPermission(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/categories/add")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(addCategoryRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Danh mục đã tồn tại")));
    }
    @Test
    void testUpdateCategory_Success() throws Exception {
        AddCategoryRequestDTO updateCategoryRequestDTO = new AddCategoryRequestDTO();
        updateCategoryRequestDTO.setCategoryName("Updated Category");

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Cập nhật danh mục thành công")
                .data(null)
                .build();

        when(categoryService.updateCategories(anyString(), anyString(), any(AddCategoryRequestDTO.class)))
                .thenReturn(ResponseEntity.ok(response));
        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("userId");
        when(categoryService.checkAdminPermission(anyString())).thenReturn(true);

        mockMvc.perform(put("/api/v1/categories/{categoryId}", "60c72b2f9b1e8b3a6c8e7c1d")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateCategoryRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Cập nhật danh mục thành công")));
    }
    @Test
    void testDeleteCategory_Success() throws Exception {
        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Xóa danh mục thành công")
                .data(null)
                .build();

        when(categoryService.deleteCategory(anyString(), anyString())).thenReturn(ResponseEntity.ok(response));
        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn("userId");
        when(categoryService.checkAdminPermission(anyString())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/categories/{categoryId}", "60c72b2f9b1e8b3a6c8e7c1d")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Xóa danh mục thành công")));
    }

}

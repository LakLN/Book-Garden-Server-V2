package com.example.bookgarden.controller;

import com.example.bookgarden.dto.AddCategoryRequestDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //Get all categories
    @GetMapping("")
    public ResponseEntity<GenericResponse> getAllCategories(){
        return categoryService.getAllCategories();
    }

    //Get categories by id
    @GetMapping("/{categoryId}")
    public  ResponseEntity<?> getCategoryById(@PathVariable String categoryId){
        return categoryService.getCategoryById(categoryId);
    }
    //Add category
    @PostMapping("/add")
    public ResponseEntity<GenericResponse> addCategory(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody AddCategoryRequestDTO addCategoryRequestDTO,
                                                       BindingResult bindingResult){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : errors) {
                String errorMessage = error.getDefaultMessage();
                errorMessages.add(errorMessage);
            }
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        return categoryService.addCategory(userId, addCategoryRequestDTO);
    }

    //Update category
    @PutMapping("/{categoryId}")
    public ResponseEntity<GenericResponse> updateCategories(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody AddCategoryRequestDTO updateCategoryRequestDTO,
                                                            @PathVariable String categoryId, BindingResult bindingResult){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : errors) {
                String errorMessage = error.getDefaultMessage();
                errorMessages.add(errorMessage);
            }
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        return categoryService.updateCategories(userId, categoryId, updateCategoryRequestDTO);
    }

    //Delete category
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<GenericResponse> deleteCategories(@RequestHeader("Authorization") String authorizationHeader,
                                                            @PathVariable String categoryId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return categoryService.deleteCategory(userId, categoryId);
    }
}

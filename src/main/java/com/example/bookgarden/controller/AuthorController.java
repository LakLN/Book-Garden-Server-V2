package com.example.bookgarden.controller;

import com.example.bookgarden.dto.AddCategoryRequestDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UpdateAuthorRequestDTO;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {
    @Autowired
    private AuthorService authorService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    //Get all authors
    @GetMapping("")
    public ResponseEntity<GenericResponse> getAllAuthors(){
        return authorService.getAllAuthors();
    }
    @PostMapping("/add")
    public ResponseEntity<GenericResponse> addAuthor(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody UpdateAuthorRequestDTO addAuthorRequestDTO,
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
        return authorService.addAuthor(userId, addAuthorRequestDTO);
    }
    @GetMapping("/{authorId}")
    public ResponseEntity<GenericResponse> getAuthorById(@PathVariable String authorId){
        return authorService.getAuthorById(authorId);
    }

    @PutMapping("/{authorId}")
    public ResponseEntity<GenericResponse> updateAuthor(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String authorId,
                                                        @Valid @RequestBody UpdateAuthorRequestDTO updateAuthorRequestDTO, BindingResult bindingResult){
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
        return authorService.updateAuthor(userId, authorId, updateAuthorRequestDTO);
    }
}

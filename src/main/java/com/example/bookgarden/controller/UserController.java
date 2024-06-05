package com.example.bookgarden.controller;

import com.example.bookgarden.dto.AddressesRequestDTO;
import com.example.bookgarden.dto.ChangePasswordRequestDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UpdateProfileRequestDTO;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import com.example.bookgarden.repository.UserRepository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    private UserService userService;

    //Get Profile
    @GetMapping("/profile")
    public ResponseEntity<GenericResponse> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.getProfile(userId);
    }

    //Update Profile
    @PostMapping("/profile/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authorizationHeader,
                                           @Valid @ModelAttribute UpdateProfileRequestDTO updateProfileRequestDTO,
                                           MultipartHttpServletRequest avatarRequest, BindingResult bindingResult){
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
        return userService.updateProfile(userId, updateProfileRequestDTO, avatarRequest);
    }

    //Change Password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO, BindingResult bindingResult){
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
        if (!changePasswordRequestDTO.getPassWord().equals(changePasswordRequestDTO.getConfirmPassWord())) {
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu nhắc lại không khớp")
                    .data(null)
                    .build());
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.changePassword(userId, changePasswordRequestDTO);
    }

    //Update Addresses
    @PutMapping ("/profile/updateAddresses")
    public ResponseEntity<?> updateAddresses(@RequestHeader("Authorization") String authorizationHeader, @RequestBody AddressesRequestDTO addressesRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.updateAddresses(userId, addressesRequestDTO);
    }

}
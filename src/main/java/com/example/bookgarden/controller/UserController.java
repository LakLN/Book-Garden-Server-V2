package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Notification;
import com.example.bookgarden.entity.SearchHistory;
import com.example.bookgarden.exception.ItemNotFoundException;
import com.example.bookgarden.repository.NotificationRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.NotificationService;
import com.example.bookgarden.service.SearchHistoryService;
import com.example.bookgarden.service.UserService;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import com.example.bookgarden.repository.UserRepository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SearchHistoryService searchHistoryService;
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
                                           MultipartHttpServletRequest avatarRequest, BindingResult bindingResult) {
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
                    .message("Dữ liệu đầu vào không hợp lệ!")
                    .data(errorMessages)
                    .build());
        }

        return userService.updateProfile(userId, updateProfileRequestDTO, avatarRequest);
    }

    //Change Password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authorizationHeader,
                                            @Valid @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        if (changePasswordRequestDTO.getPassWord().equals(changePasswordRequestDTO.getOldPassWord())) {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu mới không được giống với mật khẩu cũ!")
                    .data(null)
                    .build());
        }
        if (!changePasswordRequestDTO.getPassWord().equals(changePasswordRequestDTO.getConfirmPassWord())) {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
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
    @PutMapping("/profile/updateAddresses")
    public ResponseEntity<?> updateAddresses(@RequestHeader("Authorization") String authorizationHeader, @RequestBody AddressesRequestDTO addressesRequestDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.updateAddresses(userId, addressesRequestDTO);
    }

    //Save Search History
    @PostMapping("/search-history/save")
    public ResponseEntity<?> saveSearchHistory(@RequestHeader("Authorization") String authorizationHeader, @RequestBody SearchHistoryRequestDTO searchHistoryRequestDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        SearchHistory searchHistory = searchHistoryService.saveSearchHistory(userId, searchHistoryRequestDTO.getSearchQuery());

        return ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Lưu lịch sử tìm kiếm thành công")
                .data(searchHistory)
                .build());
    }
    // Get Search History
    @GetMapping("/search-history")
    public ResponseEntity<GenericResponse> getSearchHistoryByUser(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        List<String> searchQueries = searchHistoryService.getSearchHistoryByUserId(userId);

        return ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Lấy lịch sử tìm kiếm thành công")
                .data(searchQueries)
                .build());
    }
    @DeleteMapping("/search-history/delete")
    public ResponseEntity<GenericResponse> deleteSearchHistoryItem(@RequestHeader("Authorization") String authorizationHeader,
                                                                   @RequestBody SearchHistoryRequestDTO searchHistoryRequestDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);

        try {
            searchHistoryService.deleteSearchHistoryItem(userId, searchHistoryRequestDTO.getSearchQuery());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Xóa mục lịch sử tìm kiếm thành công")
                    .data(null)
                    .build());
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa mục lịch sử tìm kiếm")
                    .data(e.getMessage())
                    .build());
        }
    }
}
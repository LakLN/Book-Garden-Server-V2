package com.example.bookgarden.controller;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.NotificationRequestDTO;
import com.example.bookgarden.entity.Notification;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.NotificationService;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //Get notification
    @GetMapping("")
    ResponseEntity<GenericResponse> getNotifications (@RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return notificationService.getNotifications(userId);
    }
    //Create Notifications for all user
    @PostMapping("/create-for-all")
    public ResponseEntity<GenericResponse> createNotificationForAll(@RequestHeader("Authorization") String authorizationHeader,
                                                                    @Valid @RequestBody NotificationRequestDTO notificationRequestDTO,
                                                                    BindingResult bindingResult){
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
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return notificationService.createNotificationForAll(userId, notificationRequestDTO);
    }
    //Mark as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<GenericResponse> markAsRead(@RequestHeader("Authorization") String authorizationHeader,
                                                      @PathVariable String notificationId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return notificationService.markAsRead(userId, notificationId);
    }
    @GetMapping("/admin")
    public ResponseEntity<GenericResponse> getAdminNotifications(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return notificationService.getAdminNotifications(userId);
    }


}

package com.example.bookgarden.service;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.NotificationRequestDTO;
import com.example.bookgarden.entity.Notification;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.exception.AccessDeniedException;
import com.example.bookgarden.exception.NotFoundException;
import com.example.bookgarden.repository.NotificationRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public ResponseEntity<GenericResponse> getNotifications(String userId) {
        try{
            Optional<User> optionalUser = userRepository.findById(userId);
            if(optionalUser.isEmpty()){
                return ResponseEntity.badRequest().body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy người dùng")
                        .data(null)
                        .build());
            }
            List<Notification> notifications = notificationRepository.findByUserId(userId);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách thông báo thành công")
                    .data(notifications)
                    .build());
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách thông báo: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    public Notification createNotification(String userId, String title, String message, String url) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        if (url!=null) {
            notification.setUrl(url);
        }
        return notificationRepository.save(notification);
    }
    public ResponseEntity<GenericResponse> createNotificationForAll(String userId, NotificationRequestDTO notificationRequestDTO){
        try {
            checkAdminAndManagerPermission(userId);
            List<User> customers = userRepository.findAllCustomerUsers();
            System.out.println(customers);
            for (User customer : customers) {
                Notification createdNotification = createNotification(customer.getId(), notificationRequestDTO.getTitle(), notificationRequestDTO.getMessage(), "");
                simpMessagingTemplate.convertAndSend("/topic/notifications/" + customer.getId(), createdNotification);
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Tạo thông báo cho tất cả người dùng thành công")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi tạo thông báo cho tất cả người dùng: " + e.getMessage())
                    .data(null)
                    .build());
        }

    }
    @Transactional
    public ResponseEntity<GenericResponse> markAsRead(String userId, String notificationId) {
        try {
            Optional<Notification> optionalNotification = notificationRepository.findById(new ObjectId(notificationId));
            if (optionalNotification.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy thông báo")
                        .data(null)
                        .build());
            }

            Notification notification = optionalNotification.get();

                          if (!notification.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không có quyền đánh dấu thông báo này")
                        .data(null)
                        .build());
            }

            notification.setRead(true);
            notification = notificationRepository.save(notification);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Đánh dấu đã đọc thành công")
                    .data(notification)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi đánh dấu đã đọc: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    private void checkAdminAndManagerPermission(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

        if (!"Admin".equals(user.getRole()) && !"Manager".equals(user.getRole())) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện thao tác này");
        }
    }

}

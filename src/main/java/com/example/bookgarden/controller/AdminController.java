package com.example.bookgarden.controller;

import com.example.bookgarden.dto.BlacklistRequestDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.RegisterDTO;
import com.example.bookgarden.dto.UpdatePostStatusRequestDTO;
import com.example.bookgarden.entity.BlackList;
import com.example.bookgarden.entity.Role;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin ()
@RequestMapping("/api/v1/admin")
public class AdminController{
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private BlackListService blackListService;
    @Autowired
    private PostService postService;
    @Autowired
    private StatisticService statisticService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OTPService OTPService;
    @GetMapping("/dashboard/users")
    public ResponseEntity<GenericResponse> getAllUsers(@RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.getAllUsers(userId);
    }
    @DeleteMapping("/dashboard/users/{deletedUserId}")
    public ResponseEntity<GenericResponse> deleteUser(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String deletedUserId){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.deleteUser(userId, deletedUserId);
    }
    @PostMapping("/blacklist/add")
    public ResponseEntity<GenericResponse> addUserToBlacklist(@RequestHeader("Authorization") String authorizationHeader, @RequestBody BlacklistRequestDTO blacklistRequestDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return blackListService.addUserToBlacklist(userId, blacklistRequestDTO);
    }

    @GetMapping("/blacklist")
    public ResponseEntity<GenericResponse> getBlacklist(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return blackListService.getBlacklist(userId);
    }

    @PostMapping("/blacklist/{blacklistId}/restore")
    public ResponseEntity<GenericResponse> restoreUserFromBlacklist(@RequestHeader("Authorization") String authorizationHeader,
                                                                    @PathVariable String blacklistId){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return blackListService.restoreUserFromBlacklist(userId, blacklistId);
    }

    @GetMapping("/dashboard/posts")
    public ResponseEntity<GenericResponse> getAllPosts(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.getAllPosts(userId);
    }

    @PutMapping("/dashboard/posts/{postId}")
    public ResponseEntity<GenericResponse> updatePostStatus(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String postId,
                                                            @RequestBody UpdatePostStatusRequestDTO updatePostStatusRequestDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.updatePostStatus(userId, postId, updatePostStatusRequestDTO);
    }

    @GetMapping("/statistics")
    public ResponseEntity<GenericResponse> getStatistics(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return statisticService.getStatistics(userId);
    }

    @PostMapping("/register-manager")
    public ResponseEntity<?> registerManager(@RequestHeader("Authorization") String authorizationHeader,
                                             @Valid @RequestBody RegisterDTO registerDTO, BindingResult bindingResult) {
        try {
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(token);

            if (!isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .success(false)
                        .message("Bạn không có quyền thực hiện chức năng này")
                        .data(null)
                        .build());
            }
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
            if (!registerDTO.getPassWord().equals(registerDTO.getConfirmPassWord())) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu nhắc lại không khớp")
                        .data(null)
                        .build());
            }
            Optional<User> existingUser = userRepository.findByEmail(registerDTO.getEmail());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .success(false)
                        .message("Email đã tồn tại trong hệ thống")
                        .data(null)
                        .build());
            }
            User newUser = new User();
            newUser.setFullName(registerDTO.getFullName());
            newUser.setPassWord(passwordEncoder.encode(registerDTO.getPassWord()));
            newUser.setEmail(registerDTO.getEmail());
            newUser.setPhone(registerDTO.getPhone());

            Role userRole = roleService.findByRoleName("Manager");
            newUser.setRole(userRole.getRoleName());
            newUser.setIsActive(true);
            newUser.setIsVerified(true);
            userRepository.save(newUser);
            OTPService.sendRegisterOtp(registerDTO.getEmail());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Đăng ký thành công!")
                    .data("")
                    .build());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thực hiện đăng ký tài khoản cho Manager: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    public boolean isAdmin(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.isPresent() && "Admin".equals(optionalUser.get().getRole());
    }
}

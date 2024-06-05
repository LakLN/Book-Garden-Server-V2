package com.example.bookgarden.service;

import com.example.bookgarden.dto.BlacklistRequestDTO;
import com.example.bookgarden.dto.BlacklistResponseDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UserDTO;
import com.example.bookgarden.entity.BlackList;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.BlackListRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlackListService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlackListRepository blackListRepository;

    public ResponseEntity<GenericResponse> addUserToBlacklist(String userId, BlacklistRequestDTO blacklistRequestDTO) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền thêm người dùng vào danh sách đen")
                            .data(null)
                            .build());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }
            String blacklistUserId = blacklistRequestDTO.getUserId();
            Optional<User> optionalBlacklistUser = userRepository.findById(blacklistUserId);
            if (optionalBlacklistUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }

            Optional<BlackList> existingBlacklist = blackListRepository.findByUserId(new ObjectId(blacklistUserId));
            if (existingBlacklist.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng đã tồn tại trong danh sách đen")
                        .data(null)
                        .build());
            }

            BlackList blackList = new BlackList();
            blackList.setUserId(new ObjectId(blacklistUserId));
            blackList.setReason(blacklistRequestDTO.getReason());
            blackListRepository.save(blackList);
            User blackListUser = optionalBlacklistUser.get();
            blackListUser.setIsActive(false);
            userRepository.save(optionalBlacklistUser.get());

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Người dùng đã được thêm vào danh sách đen")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thêm người dùng vào danh sách đen")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getBlacklist(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền thêm người dùng vào danh sách đen")
                            .data(null)
                            .build());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }
            List<BlackList> blackList = blackListRepository.findAll();

            List<BlacklistResponseDTO> blacklistResponseDTOs = blackList.stream()
                    .map(this::convertToBlacklistDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Danh sách người dùng trong danh sách đen")
                    .data(blacklistResponseDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách đen")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> restoreUserFromBlacklist(String userId, String blacklistId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền thêm người dùng vào danh sách đen")
                            .data(null)
                            .build());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }
            Optional<BlackList> optionalBlacklist = blackListRepository.findById(blacklistId);
            if (optionalBlacklist.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Danh sách đen không tồn tại")
                        .data(null)
                        .build());
            }

            BlackList blackList = optionalBlacklist.get();
            blackListRepository.delete(blackList);

            Optional<User> optionalRestoredUser = userRepository.findById(blackList.getUserId().toString());
            if (optionalRestoredUser.isPresent()) {
                User restoredUser = optionalRestoredUser.get();
                restoredUser.setIsActive(true);
                userRepository.save(restoredUser);

                return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                        .success(true)
                        .message("Khôi phục người dùng từ danh sách đen thành công")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại trong hệ thống")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi khôi phục người dùng từ danh sách đen")
                    .data(e.getMessage())
                    .build());
        }
    }

    private BlacklistResponseDTO convertToBlacklistDTO(BlackList blackList) {
        ModelMapper modelMapper = new ModelMapper();
        BlacklistResponseDTO blacklistResponseDTO = modelMapper.map(blackList, BlacklistResponseDTO.class);

        Optional<User> optionalUser = userRepository.findById(blackList.getUserId().toString());
        optionalUser.ifPresent(user -> blacklistResponseDTO.setUserInfo(convertToUserDTO(user)));

        return blacklistResponseDTO;
    }

    private UserDTO convertToUserDTO(User user) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(user, UserDTO.class);
    }
}
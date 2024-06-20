package com.example.bookgarden.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Address;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.AddressRepository;
import com.example.bookgarden.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.util.stream.Collectors;
import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Optional<User> findByEmail(String email) {return userRepository.findByEmail(email);}
    @Transactional
    public ResponseEntity<GenericResponse> getProfile(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message("User not found")
                    .data(null)
                    .build());
        }

        User user = optionalUser.get();
        ModelMapper modelMapper = new ModelMapper();
        List<Address> addresses = getAddressList(user.getAddresses());

        UserDTO userResponse = modelMapper.map(user, UserDTO.class);
        userResponse.setAddresses(addresses);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Successfully retrieved user profile")
                        .data(userResponse)
                        .build()
        );
    }

    public List<Address> getAddressList(List<String> addressIds) {
        List<Address> addresses = new ArrayList<>();
        for (String addressId : addressIds) {
            Optional<Address> optionalAddress = addressRepository.findById(addressId);
            optionalAddress.ifPresent(addresses::add);
        }
        return addresses;
    }

    public ResponseEntity<GenericResponse> changePassword(String userId, ChangePasswordRequestDTO changePasswordRequestDTO){
        Optional<User> userOptional = userRepository.findById(userId);
        try {
            if (userOptional.isPresent()){
                User user = userOptional.get();
                user.setPassWord(passwordEncoder.encode(changePasswordRequestDTO.getPassWord()));
                userRepository.save(user);
                return ResponseEntity.ok(
                        GenericResponse.builder()
                                .success(true)
                                .message("Đổi mật khẩu thành công")
                                .data(null)
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy người dùng")
                                .data(null)
                                .build()
                );
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi mật khẩu không thành công")
                    .data(null)
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> updateProfile(String userId, UpdateProfileRequestDTO updateProfileRequestDTO, MultipartHttpServletRequest avatarRequest) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("User not found")
                            .data(null)
                            .build()
            );

        User user = optionalUser.get();
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(updateProfileRequestDTO, user);

        MultipartFile avatar = avatarRequest.getFile("avatar");
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String imageUrl = cloudinary.uploader().upload(avatar.getBytes(), ObjectUtils.emptyMap()).get("secure_url").toString();
                user.setAvatar(imageUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                        .success(false)
                        .message("Error uploading image")
                        .data(null)
                        .build());
            }
        }

        userRepository.save(user);
        List<Address> addresses = new ArrayList<>();
        for (String addressId : user.getAddresses()) {
            Optional<Address> optionalAddress = addressRepository.findById(addressId);
            optionalAddress.ifPresent(addresses::add);
        }

        UserDTO userResponse = modelMapper.map(user, UserDTO.class);
        userResponse.setAddresses(addresses);

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("User profile updated successfully")
                        .data(userResponse)
                        .build()
        );
    }


    public ResponseEntity<GenericResponse> updateAddresses(String userId, AddressesRequestDTO addressesRequestDTO) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Không tìm thấy người dùng")
                            .data(null)
                            .build()
            );
        }

        User user = optionalUser.get();
        List<Address> newAddresses = new ArrayList<>();

        for (String address : addressesRequestDTO.getAddresses()) {
            Optional<Address> optionalExistingAddress = addressRepository.findByAddress(address);

            if (optionalExistingAddress.isEmpty()) {
                Address newAddress = new Address();
                newAddress.setAddress(address);
                addressRepository.save(newAddress);
                newAddresses.add(newAddress);
            } else {
                Address existingAddress = optionalExistingAddress.get();
                newAddresses.add(existingAddress);
            }
        }

        user.setAddresses(newAddresses.stream().map(Address::getId).collect(Collectors.toList()));
        User updatedUser = userRepository.save(user);

        List<Address> addresses = new ArrayList<>();
        for (String addressId : updatedUser.getAddresses()) {
            Optional<Address> optionalAddress = addressRepository.findById(addressId);
            optionalAddress.ifPresent(addresses::add);
        }
        ModelMapper modelMapper = new ModelMapper();
        UserDTO userResponse = modelMapper.map(updatedUser, UserDTO.class);
        userResponse.setAddresses(addresses);

        return ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Cập nhật địa chỉ người dùng thành công")
                .data(userResponse)
                .build());
    }

    public ResponseEntity<GenericResponse> getAllUsers(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền lấy danh sách người dùng")
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
            List<User> users = userRepository.findByIsActiveTrue();

            List<UserDashboardResponseDTO> userDashboardResponseDTOS = users.stream()
                    .map(this::convertToUserDashboardDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách người dùng thành công")
                    .data(userDashboardResponseDTOS)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách người dùng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteUser(String userId, String deletedUserId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền xóa người dùng")
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

            Optional<User> optionalUser1 = userRepository.findById(deletedUserId);
            if(optionalUser1.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }
            User deletedUser = optionalUser1.get();
            deletedUser.setIsActive(false);
            deletedUser = userRepository.save(deletedUser);
            UserDashboardResponseDTO userDTO = convertToUserDashboardDTO(deletedUser);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Xóa người dùng thành công")
                    .data(userDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa người dùng")
                    .data(e.getMessage())
                    .build());
        }
    }
    public boolean changePassword(User user, String newPassword) {
        try {
            user.setPassWord(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private UserDashboardResponseDTO convertToUserDashboardDTO(User user){
        ModelMapper modelMapper = new ModelMapper();
        UserDashboardResponseDTO userResponse = modelMapper.map(user, UserDashboardResponseDTO.class);
        return userResponse;
    }
    @Transactional
    public void deleteUnverifiedAccounts() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date twentyFourHoursAgo = calendar.getTime();

        List<User> unverifiedAccounts = userRepository.findByIsVerifiedFalseAndCreatedAtBefore(twentyFourHoursAgo);
        userRepository.deleteAll(unverifiedAccounts);
    }
    @PostConstruct
    public void init() {
        deleteUnverifiedAccounts();
    }
    @Scheduled(fixedDelay = 86400000) // 24 hours
    public void scheduledDeleteUnverifiedAccounts() {
        deleteUnverifiedAccounts();
    }
}
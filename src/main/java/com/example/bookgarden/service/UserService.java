package com.example.bookgarden.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Address;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.AddressRepository;
import com.example.bookgarden.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
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
                    .message("Không tìm thấy người dùng!")
                    .data(null)
                    .build());
        }

        User user = optionalUser.get();
        ModelMapper modelMapper = new ModelMapper();
        List<Address> addresses = getAddressList(user.getAddresses());

        UserDTO userResponse = modelMapper.map(user, UserDTO.class);
        userResponse.setAddresses(addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Lấy thông tin người dùng thành công!")
                        .data(userResponse)
                        .build()
        );
    }

    public List<Address> getAddressList(List<ObjectId> addressIds) {
        List<Address> addresses = new ArrayList<>();
        for (ObjectId addressId : addressIds) {
            Optional<Address> optionalAddress = addressRepository.findById(addressId);
            optionalAddress.ifPresent(addresses::add);
        }
        return addresses;
    }


    public ResponseEntity<GenericResponse> changePassword(String userId, ChangePasswordRequestDTO changePasswordRequestDTO) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Không tìm thấy người dùng")
                            .data(null)
                            .build()
            );
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassWord(), user.getPassWord())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Mật khẩu cũ không chính xác!")
                            .data(null)
                            .build()
            );
        }

        user.setPassWord(passwordEncoder.encode(changePasswordRequestDTO.getPassWord()));
        try {
            userRepository.save(user);
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Đổi mật khẩu thành công")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Lỗi khi đổi mật khẩu")
                            .data(e.getMessage())
                            .build()
            );
        }
    }


    public ResponseEntity<GenericResponse> updateProfile(String userId, UpdateProfileRequestDTO updateProfileRequestDTO, MultipartHttpServletRequest avatarRequest) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    GenericResponse.builder()
                            .success(false)
                            .message("Không tìm thấy người dùng")
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
                        .message("Upload ảnh thất bại")
                        .data(null)
                        .build());
            }
        }

        userRepository.save(user);
        List<Address> addresses = new ArrayList<>();
        for (ObjectId addressId : user.getAddresses()) {
            Optional<Address> optionalAddress = addressRepository.findById(addressId);
            optionalAddress.ifPresent(addresses::add);
        }

        UserDTO userResponse = modelMapper.map(user, UserDTO.class);
        userResponse.setAddresses(addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật thông tin người dùng thành công!")
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
        List<ObjectId> oldAddressIds = new ArrayList<>(user.getAddresses());
        List<Address> newAddresses = new ArrayList<>();
        boolean defaultAddressFound = false;

        for (AddressDTO addressDTO : addressesRequestDTO.getAddresses()) {
            Address addressEntity = addressRepository.findByNameAndPhoneNumberAndAddress(
                            addressDTO.getName(),
                            addressDTO.getPhoneNumber(),
                            addressDTO.getAddress())
                    .orElseGet(() -> {
                        Address newAddress = new Address();
                        newAddress.setName(addressDTO.getName());
                        newAddress.setPhoneNumber(addressDTO.getPhoneNumber());
                        newAddress.setAddress(addressDTO.getAddress());
                        newAddress.setIsDefault(addressDTO.getIsDefault());
                        return addressRepository.save(newAddress);
                    });

            if (addressDTO.getIsDefault() && defaultAddressFound==false) {
                defaultAddressFound = true;
                addressEntity.setIsDefault(true);
            } else {
                addressEntity.setIsDefault(false);
            }
            addressRepository.save(addressEntity); // Ensure the entity is saved with the correct default flag
            newAddresses.add(addressEntity);
        }

        if (!defaultAddressFound && !newAddresses.isEmpty()) {
            Address firstAddress = newAddresses.get(0);
            firstAddress.setIsDefault(true);
            addressRepository.save(firstAddress);
        }

        List<ObjectId> newAddressIds = newAddresses.stream().map(Address::getId).collect(Collectors.toList());
        user.setAddresses(newAddressIds);
        User updatedUser = userRepository.save(user);

        for (ObjectId addressId : oldAddressIds) {
            if (!newAddressIds.contains(addressId)) {
                addressRepository.deleteById(addressId.toString());
            }
        }

        List<Address> addresses = getAddressList(updatedUser.getAddresses());

        ModelMapper modelMapper = new ModelMapper();
        UserDTO userResponse = modelMapper.map(updatedUser, UserDTO.class);
        userResponse.setAddresses(addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList()));

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
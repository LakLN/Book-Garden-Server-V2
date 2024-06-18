package com.example.bookgarden.service;

import com.example.bookgarden.dto.RegisterDTO;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.dto.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OTPService otpService;

    public GenericResponse registerUser(RegisterDTO registerDTO) {
        Optional<User> existingUser = userRepository.findByEmail(registerDTO.getEmail());
        if (existingUser.isPresent()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Email đã tồn tại trong hệ thống")
                    .data(null)
                    .build();
        }

        User newUser = new User();
        newUser.setFullName(registerDTO.getFullName());
        newUser.setPassWord(passwordEncoder.encode(registerDTO.getPassWord()));
        newUser.setEmail(registerDTO.getEmail());
        newUser.setPhone(registerDTO.getPhone());
        newUser.setRole(roleService.findByRoleName("Customer").getRoleName());

        userRepository.save(newUser);
        otpService.sendRegisterOtp(registerDTO.getEmail());

        return GenericResponse.builder()
                .success(true)
                .message("Đăng ký thành công!")
                .data(null)
                .build();
    }
}

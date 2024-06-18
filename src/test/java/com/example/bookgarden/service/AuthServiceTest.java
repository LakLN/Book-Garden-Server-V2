package com.example.bookgarden.service;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.RegisterDTO;
import com.example.bookgarden.entity.Role;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.service.OTPService;
import com.example.bookgarden.service.AuthService;
import com.example.bookgarden.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OTPService otpService;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFullName("John Doe");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPhone("0123456789");
        registerDTO.setPassWord("password");
        registerDTO.setConfirmPassWord("password");

        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(roleService.findByRoleName("Customer")).thenReturn(new Role(null, "Customer"));

        GenericResponse result = authService.registerUser(registerDTO);

        assertEquals("Đăng ký thành công!", result.getMessage());

        verify(userRepository, times(1)).save(any(User.class));
        verify(otpService, times(1)).sendRegisterOtp(registerDTO.getEmail());
    }
}

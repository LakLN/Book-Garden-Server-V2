package com.example.bookgarden.service;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.LoginDTO;
import com.example.bookgarden.dto.RegisterDTO;
import com.example.bookgarden.entity.Role;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.security.UserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setFullName("Tester 01");
        registerDTO.setEmail("test01@example.com");
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

    @Test
    public void testLogin_Success() {
        // Setup mock responses
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password123");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassWord("password123");
        user.setIsVerified(true);
        user.setIsActive(true);

        Authentication authentication = mock(Authentication.class);
        UserDetail userDetail = mock(UserDetail.class);

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetail);
        when(userDetail.getUserId()).thenReturn("user-id");
        when(jwtTokenProvider.generateAccessToken(any(UserDetail.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(any(UserDetail.class))).thenReturn("refreshToken");

        // Call service
        GenericResponse response = authService.login(loginDTO);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successfully!", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    public void testLogin_UserNotFound() {
        LoginDTO loginDTO = new LoginDTO("notfound@example.com", "password123");

        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());

        GenericResponse response = authService.login(loginDTO);

        assertFalse(response.isSuccess());
        assertEquals("Tài khoản không tồn tại", response.getMessage());
    }

    @Test
    public void testLogin_UserNotVerified() {
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password123");
        User user = new User();
        user.setEmail("test@example.com");
        user.setIsVerified(false);

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));

        GenericResponse response = authService.login(loginDTO);

        assertFalse(response.isSuccess());
        assertEquals("Tài khoản chưa được xác thực", response.getMessage());
    }

    @Test
    public void testLogin_UserNotActive() {
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password123");
        User user = new User();
        user.setEmail("test@example.com");
        user.setIsVerified(true);
        user.setIsActive(false);

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));

        GenericResponse response = authService.login(loginDTO);

        assertFalse(response.isSuccess());
        assertEquals("Tài khoản đã bị vô hiệu hóa", response.getMessage());
    }
}

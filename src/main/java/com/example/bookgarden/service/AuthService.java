package com.example.bookgarden.service;

import com.example.bookgarden.dto.ForgotPasswordDTO;
import com.example.bookgarden.dto.LoginDTO;
import com.example.bookgarden.dto.RegisterDTO;
import com.example.bookgarden.entity.Token;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.security.UserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OTPService otpService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TokenService tokenService;

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
    public GenericResponse forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        if (!forgotPasswordDTO.getPassWord().equals(forgotPasswordDTO.getConfirmPassWord())) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu nhắc lại không khớp")
                    .data(null)
                    .build();
        }

        Optional<User> existingUser = userRepository.findByEmail(forgotPasswordDTO.getEmail());
        if (!existingUser.isPresent()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Email không tồn tại trong hệ thống")
                    .data(null)
                    .build();
        }

        User user = existingUser.get();
        boolean isPasswordChanged = userService.changePassword(user, forgotPasswordDTO.getPassWord());

        if (isPasswordChanged) {
            return GenericResponse.builder()
                    .success(true)
                    .message("Password reset successfully!")
                    .data(null)
                    .build();
        } else {
            return GenericResponse.builder()
                    .success(false)
                    .message("An error occurred while resetting password.")
                    .data(null)
                    .build();
        }
    }

    public GenericResponse login(LoginDTO loginDTO) {
        Optional<User> optionalUser = userService.findByEmail(loginDTO.getEmail());
        if (!optionalUser.isPresent()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản không tồn tại")
                    .data(null)
                    .build();
        }

        User user = optionalUser.get();
        if (!user.getIsVerified()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản chưa được xác thực")
                    .data(null)
                    .build();
        }

        if (!user.getIsActive()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản đã bị vô hiệu hóa")
                    .data(null)
                    .build();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassWord()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetail userDetail = (UserDetail) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
        Token refreshToken = new Token();
        String token = jwtTokenProvider.generateRefreshToken(userDetail);
        refreshToken.setToken(token);
        refreshToken.setUserId(userDetail.getUserId());

        tokenService.save(refreshToken);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", token);

        return GenericResponse.builder()
                .success(true)
                .message("Login successfully!")
                .data(tokenMap)
                .build();
    }
}

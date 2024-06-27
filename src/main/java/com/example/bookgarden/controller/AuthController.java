package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private OTPService otpService;
    @Autowired
    private AuthService authService;

    @GetMapping("/loginGoogle")
    public ModelAndView login() {
        return new ModelAndView("redirect:" + authService.getGoogleLoginUrl());
    }

    @GetMapping("/loginGoogle/oauth2/code/google")
    public ResponseEntity<GenericResponse> handleGoogleCallback(@RequestParam("code") String code) {
        GenericResponse response = authService.handleGoogleCallback(code);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDTO registerDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ!")
                    .data(errorMessages)
                    .build());
        }

        if (!registerDTO.getPassWord().equals(registerDTO.getConfirmPassWord())) {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu không khớp")
                    .data(null)
                    .build());
        }

        GenericResponse response = authService.registerUser(registerDTO);
        if (!response.isSuccess()) {
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/send-register-otp")
    public ResponseEntity<GenericResponse> sendRegisterOtp(@RequestBody OTPRequest otpRequest) {
        try {
            otpService.sendRegisterOtp(otpRequest.getEmail());
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Gửi mã OTP thành công!")
                            .data(null)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Có lỗi trong khi gửi mã OTP.")
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/send-forgot-password-otp")
    public ResponseEntity<GenericResponse> sendForgotPasswordOtp(@RequestBody OTPRequest otpRequest) {
        try {
            otpService.sendForgotPasswordOtp(otpRequest.getEmail());
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("Gửi mã OTP thành công!")
                            .data(null)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Có lỗi trong khi gửi mã OTP.")
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/verify-OTP")
    public ResponseEntity<GenericResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        boolean isOtpVerified = otpService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        if (isOtpVerified) {
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Xác thực mã OTP thành công!")
                    .data(null)
                    .build());
        } else {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Mã OTP không chính xác hoặc đã hết hạn!")
                    .data(null)
                    .build());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponse> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ!")
                    .data(errorMessages)
                    .build());
        }

        GenericResponse response = authService.forgotPassword(forgotPasswordDTO);
        if (!response.isSuccess()) {
            return ResponseEntity.status(400).body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ!")
                    .data(errorMessages)
                    .build());
        }

        GenericResponse response = authService.login(loginDTO);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse> logout(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody TokenRequestDTO tokenRequestDTO) {
        return authService.logout(authorizationHeader, tokenRequestDTO);
    }
    @PostMapping("/logout-all")
    public ResponseEntity<GenericResponse> logoutAll(@RequestHeader("Authorization") String authorizationHeader) {
        return authService.logoutAll(authorizationHeader);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<GenericResponse> refreshAccessToken(@RequestBody TokenRequestDTO tokenRequestDTO) {
        String refreshToken = tokenRequestDTO.getRefreshToken();
        return tokenService.refreshAccessToken(refreshToken);
    }
}

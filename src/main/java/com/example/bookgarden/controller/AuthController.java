package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Role;
import com.example.bookgarden.entity.Token;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.security.UserDetail;
import com.example.bookgarden.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private OTPService otpService;
    @Autowired
    private AuthService authService;
    private final String clientId = "334650103063-k5oinc902jh7nsk2nd709va5bhh9961f.apps.googleusercontent.com";
    private final String clientSecret = "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";
    private final String redirectUri = "http://localhost:8081/api/v1/auth/login/oauth2/code/google";
    private final String tokenUri = "https://accounts.google.com/o/oauth2/token";

    @GetMapping("/loginGoogle")
    public ModelAndView login() {
        String clientId = "3334650103063-k5oinc902jh7nsk2nd709va5bhh9961f.apps.googleusercontent.com";
        String redirectUri = "http://localhost:8081/api/v1/auth/login/oauth2/code/google";
        String url = "https://accounts.google.com/o/oauth2/auth?client_id=" + clientId +
                "&redirect_uri=" + redirectUri + "&scope=email%20profile&response_type=code";

        return new ModelAndView("redirect:" + url);
    }

    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<String> handleGoogleCallback(@RequestParam("code") String code) {
        String accessToken = exchangeCodeForToken(code);

        return ResponseEntity.ok("Authentication successful! Access Token: " + accessToken);
    }

    private String extractAccessToken(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("access_token")) {
                String accessToken = jsonNode.get("access_token").asText();
                return accessToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tokenUri)
                .queryParam("code", code)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("grant_type", "authorization_code");

        ResponseEntity<String> response = restTemplate.postForEntity(builder.toUriString(), null, String.class);

        String responseBody = response.getBody();
        String accessToken = extractAccessToken(responseBody);
        return accessToken;
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
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }

        if (!registerDTO.getPassWord().equals(registerDTO.getConfirmPassWord())) {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu nhắc lại không khớp")
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
                            .message("OTP sent successfully!")
                            .data(null)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("An error occurred while sending OTP.")
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
                            .message("OTP sent successfully!")
                            .data(null)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("An error occurred while sending OTP.")
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
                    .message("OTP verified successfully!")
                    .data(null)
                    .build());
        } else {
            return ResponseEntity.badRequest().body(GenericResponse.builder()
                    .success(false)
                    .message("Invalid OTP or expired.")
                    .data(null)
                    .build());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponse> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errorMessages = new ArrayList<>();
                for (ObjectError error : bindingResult.getAllErrors()) {
                    errorMessages.add(error.getDefaultMessage());
                }
                return ResponseEntity.badRequest().body(GenericResponse.builder()
                        .success(false)
                        .message("Dữ liệu đầu vào không hợp lệ")
                        .data(errorMessages)
                        .build());
            }

            if (!forgotPasswordDTO.getPassWord().equals(forgotPasswordDTO.getConfirmPassWord())) {
                return ResponseEntity.badRequest().body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu nhắc lại không khớp")
                        .data(null)
                        .build());
            }

            Optional<User> existingUser = userRepository.findByEmail(forgotPasswordDTO.getEmail());
            if (!existingUser.isPresent()) {
                return ResponseEntity.badRequest().body(GenericResponse.builder()
                        .success(false)
                        .message("Email không tồn tại trong hệ thống")
                        .data(null)
                        .build());
            }

            otpService.sendForgotPasswordOtp(forgotPasswordDTO.getEmail());
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("OTP sent successfully!")
                            .data(null)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("An error occurred while sending OTP.")
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult bindingResult) {
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
        Optional<User> optionalUser = userService.findByEmail(loginDTO.getEmail());
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản không tồn tại")
                    .data(null)
                    .build());
        }
        User user = optionalUser.get();
        if(user.getIsVerified()==false){
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Tài khoản chưa được xác thực")
                    .data(null)
                    .build());
        }
        if(user.getIsActive()==false){
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Tài khoản đã bị vô hiệu hóa")
                    .data(null)
                    .build());
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),
                        loginDTO.getPassWord()));
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

        return ResponseEntity.ok().body(GenericResponse.builder()
                .success(true)
                .message("Login successfully!")
                .data(tokenMap)
                .build());
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader,
                                    @RequestBody TokenRequestDTO tokenRequestDTO) {
        String accessToken = authorizationHeader.substring(7);
        if (jwtTokenProvider.validateToken(accessToken) && jwtTokenProvider.validateToken(tokenRequestDTO.getRefreshToken())) {
            String userIdFromAccessToken = jwtTokenProvider.getUserIdFromJwt(accessToken);
            String userIdFromRefreshToken = jwtTokenProvider.getUserIdFromRefreshToken(tokenRequestDTO.getRefreshToken());
            if (userIdFromAccessToken.equals(userIdFromRefreshToken)) {
                return tokenService.logout(tokenRequestDTO.getRefreshToken());
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Logout failed!")
                        .data("Please login before logout!")
                        .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRequestDTO tokenRequestDTO) {
        String refreshToken = tokenRequestDTO.getRefreshToken();
        return tokenService.refreshAccessToken(refreshToken);
    }

}

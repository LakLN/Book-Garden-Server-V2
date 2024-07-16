package com.example.bookgarden.service;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.*;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.security.UserDetail;
import com.example.bookgarden.security.UserDetailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
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
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.redirect.uri}")
    private String redirectUri;
    @Value("${google.token.uri}")
    private String tokenUri;

    private String userInfoUri = "https://www.googleapis.com/oauth2/v2/userinfo";
    public GenericResponse registerUser(RegisterDTO registerDTO) {
        Optional<User> existingUser = userRepository.findByEmail(registerDTO.getEmail());
        if (existingUser.isPresent()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Email đã tồn tại!")
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
        String notificationMessage = "Chào mừng bạn đến với Book Garden! Cảm ơn bạn đã đăng ký, chúng tôi hy vọng bạn sẽ có những trải nghiệm tuyệt vời!";
        Notification notification = notificationService.createNotification(newUser.getId().toString(), "Chào mừng", notificationMessage, "/", "");
        messagingTemplate.convertAndSend("/topic/notifications/" + newUser.getId().toString(), notification);
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
                    .message("Mật khẩu không khớp!")
                    .data(null)
                    .build();
        }

        Optional<User> existingUser = userRepository.findByEmail(forgotPasswordDTO.getEmail());
        if (!existingUser.isPresent()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Email không tồn tại trong hệ thống!")
                    .data(null)
                    .build();
        }

        User user = existingUser.get();
        boolean isPasswordChanged = userService.changePassword(user, forgotPasswordDTO.getPassWord());

        if (isPasswordChanged) {
            return GenericResponse.builder()
                    .success(true)
                    .message("Đổi mật khẩu thành công!")
                    .data(null)
                    .build();
        } else {
            return GenericResponse.builder()
                    .success(false)
                    .message("Có lỗi trong khi thực hiện đổi mật khẩu.")
                    .data(null)
                    .build();
        }
    }

    public GenericResponse login(LoginDTO loginDTO) {
        Optional<User> optionalUser = userService.findByEmail(loginDTO.getEmail());
        if (!optionalUser.isPresent()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản không tồn tại!")
                    .data(null)
                    .build();
        }

        User user = optionalUser.get();
        if (!user.getIsVerified()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản chưa được xác thực!")
                    .data(null)
                    .build();
        }

        if (!user.getIsActive()) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Tài khoản đã bị vô hiệu hóa!")
                    .data(null)
                    .build();
        }
        try {
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
                    .message("Đăng nhập thành công!")
                    .data(tokenMap)
                    .build();
        } catch (BadCredentialsException ex) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu không chính xác!")
                    .data(null)
                    .build();
        } catch (Exception ex) {
            return GenericResponse.builder()
                    .success(false)
                    .message("Đã xảy ra lỗi khi đăng nhập!")
                    .data(ex.getMessage())
                    .build();
        }
    }

    public String getGoogleLoginUrl() {
        return "https://accounts.google.com/o/oauth2/auth?client_id=" + clientId +
                "&redirect_uri=" + redirectUri + "&scope=email%20profile&response_type=code";
    }

    public GenericResponse handleGoogleCallback(String code) {
        String accessToken = exchangeCodeForToken(code);
        GoogleUserInfo googleUserInfo = getUserInfo(accessToken);

        Optional<User> existingUser = userRepository.findByEmail(googleUserInfo.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            return loginUser(user);
        } else {
            User newUser = new User();
            newUser.setEmail(googleUserInfo.getEmail());
            newUser.setFullName(googleUserInfo.getName());
            newUser.setAvatar(googleUserInfo.getPicture());
            newUser.setIsVerified(true);
            newUser.setIsActive(true);
            newUser.setRole(roleService.findByRoleName("Customer").getRoleName());

            userRepository.save(newUser);
            return loginUser(newUser);
        }
    }

    private GenericResponse loginUser(User user) {
        UserDetail userDetail = (UserDetail) userDetailService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetail);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        return GenericResponse.builder()
                .success(true)
                .message("Đăng nhập thành công!")
                .data(tokenMap)
                .build();
    }

    public GoogleUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, GoogleUserInfo.class);
        return response.getBody();
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

        return extractAccessToken(response.getBody());
    }

    private String extractAccessToken(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("access_token")) {
                return jsonNode.get("access_token").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public ResponseEntity<GenericResponse> logout(String authorizationHeader, TokenRequestDTO tokenRequestDTO) {
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
                        .message("Đăng xuất thất bại!")
                        .data("Vui lòng đăng nhập trước khi thực hiện!")
                        .build());
    }
    public ResponseEntity<GenericResponse> logoutAll(String authorizationHeader) {
        String accessToken = authorizationHeader.substring(7);
        if (jwtTokenProvider.validateToken(accessToken)) {
            String userId = jwtTokenProvider.getUserIdFromJwt(accessToken);
            return tokenService.logoutAll(userId);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Đăng xuất thất bại!")
                        .data("Access token không chính xác!")
                        .build());
    }
}

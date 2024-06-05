package com.example.bookgarden.service;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.entity.Token;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.TokenRepository;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.security.UserDetail;
import com.example.bookgarden.security.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class TokenService {
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserDetailService userDetailService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    public <S extends Token> S save(S entity) {
        return tokenRepository.save(entity);
    }

    public ResponseEntity<GenericResponse> refreshAccessToken(String refreshToken) {
        try {
            String userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent() && optionalUser.get().getIsActive()) {
                List<Token> tokens = tokenRepository.findByUserIdOrderByCreatedAtDesc(userId);

                if (!tokens.isEmpty() && jwtTokenProvider.validateToken(tokens.get(0).getToken())) {
                    if (!tokens.get(0).getToken().equals(refreshToken)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                                .body(GenericResponse.builder()
                                        .success(false)
                                        .message("RefreshToken is not present. Please login again!")
                                        .data("")
                                        .build());
                    }

                    UserDetail userDetail = (UserDetail) userDetailService.loadUserByUserId(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken));
                    String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("accessToken", accessToken);
                    resultMap.put("refreshToken", refreshToken);

                    return ResponseEntity.status(200)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("")
                                    .data(resultMap)
                                    .build());
                }
            }
            return ResponseEntity.status(401)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Unauthorized. Please login again!")
                            .data("")
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data("")
                            .build());
        }
    }


    public ResponseEntity<?> logout(String refreshToken) {
        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                Optional<Token> optionalRefreshToken = tokenRepository.findByToken(refreshToken);
                if (optionalRefreshToken.isPresent()) {
                    tokenRepository.delete(optionalRefreshToken.get());
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(GenericResponse.builder()
                                    .success(true)
                                    .message("Logout successfully!")
                                    .data("")
                                    .build());
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenericResponse.builder()
                                .success(false)
                                .message("Invalid refresh token")
                                .data("")
                                .build());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("Invalid refresh token")
                            .data("")
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .data("")
                            .build());
        }
    }
}

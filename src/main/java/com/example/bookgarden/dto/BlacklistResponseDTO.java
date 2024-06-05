package com.example.bookgarden.dto;

import lombok.Data;

@Data
public class BlacklistResponseDTO {
    private String id;
    private UserDTO userInfo;
    private String reason;
    private String createdAt;
}
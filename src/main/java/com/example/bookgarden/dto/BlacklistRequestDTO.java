package com.example.bookgarden.dto;

import lombok.Data;

@Data
public class BlacklistRequestDTO {
    private String userId;
    private String reason;
}

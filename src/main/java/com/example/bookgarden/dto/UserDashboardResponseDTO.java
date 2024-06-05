package com.example.bookgarden.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDashboardResponseDTO {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
    private String role;
    private String phone;
    private int points;
    private Date birthday;
    private String gender;
    private Boolean isActive;
    private Boolean isVerified;
    private Date createdAt;
}

package com.example.bookgarden.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OTPRequest {
    @NotEmpty(message = "Email không được bỏ trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
}

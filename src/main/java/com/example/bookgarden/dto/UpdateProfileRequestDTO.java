package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateProfileRequestDTO {
    @NotEmpty(message = "Tên không được bỏ trống")
    private String fullName;

    @NotEmpty(message = "Số điện thoại không được bỏ trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    private Date birthday;

    private String gender;
}

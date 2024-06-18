package com.example.bookgarden.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotEmpty(message = "Tên không được bỏ trống")
    private String fullName;

    @NotEmpty(message = "Email không được bỏ trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotEmpty(message = "Số điện thoại không được bỏ trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @NotBlank(message = "Mật khẩu không được bỏ trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 kí tự")
    private String passWord;

    @NotEmpty(message = "Mật khẩu nhắc lại không được bỏ trống")
    @Size(min = 8, message = "Mật khẩu nhắc lại phải có ít nhất 8 kí tự")
    private String confirmPassWord;
}

package com.example.bookgarden.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
    @NotEmpty(message = "Email không được bỏ trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotEmpty(message = "Mật khẩu không được bỏ trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String passWord;

    @NotEmpty(message = "Mật khẩu nhắc lại không được bỏ trống")
    private String confirmPassWord;
}

package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    @NotEmpty(message = "Mật khẩu không được bỏ trống")
    private String oldPassWord;

    @NotBlank(message = "Mật khẩu không được bỏ trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 kí tự")
    private String passWord;

    @NotEmpty(message = "Mật khẩu nhắc lại không được bỏ trống")
    private String confirmPassWord;
}

package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
public class NotificationRequestDTO {
    @NotEmpty(message = "Tiêu đề thông báo không được bỏ trống")
    private String title;
    @NotEmpty(message = "Nội dung thông báo không được bỏ trống")
    private String message;
}

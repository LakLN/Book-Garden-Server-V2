package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateAuthorRequestDTO {
    @NotEmpty(message = "Tên tác giả không được bỏ trống")
    private String authorName;
}

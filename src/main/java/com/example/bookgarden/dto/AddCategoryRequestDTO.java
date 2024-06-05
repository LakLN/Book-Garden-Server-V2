package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddCategoryRequestDTO {
    @NotEmpty (message = "Tên thể loại không được bỏ trống")
    private String categoryName;
}

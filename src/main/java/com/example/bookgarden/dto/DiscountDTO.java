package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiscountDTO {
    @NotNull (message = "Giảm giá không được bỏ trống")
    private int discountPercent;
    @NotNull(message = "Ngày bắt đầu không được bỏ trống")
    private LocalDate startDate;
    @NotNull (message = "Ngày kết thúc không được bỏ trống")
    private LocalDate endDate;
}

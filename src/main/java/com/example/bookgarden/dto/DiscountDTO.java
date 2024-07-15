package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class DiscountDTO {
    @NotNull (message = "Giảm giá không được bỏ trống")
    private int discountPercent;
    @NotNull(message = "Ngày bắt đầu không được bỏ trống")
    private Date startDate;
    @NotNull (message = "Ngày kết thúc không được bỏ trống")
    private Date endDate;
}

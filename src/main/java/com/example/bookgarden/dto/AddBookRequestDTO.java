package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBookRequestDTO {
    @NotEmpty(message = "Tiêu đề sách không được bỏ trống")
    private String title;

    @NotEmpty(message = "Thể loại không được bỏ trống")
    private String  categories;

    @NotEmpty(message = "Tác giả không được bỏ trống")
    private String  authors;

    @NotNull(message = "Giá tiền không được bỏ trống")
    @Positive(message = "Giá tiền phải lớn hơn 0")
    private double price;

    @NotNull(message = "Số lượng không được bỏ trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private int stock;

    @NotEmpty(message = "Mã ISBN không được bỏ trống")
    private String isbn;

    @NotNull(message = "Số trang không được bỏ trống")
    @Positive(message = "Số trang phải lớn hơn 0")
    private int pageNumbers;

    private int soldQuantity = 0;

    private String description;

    @NotEmpty(message = "Nhà xuất bản sách không được bỏ trống")
    private String publisher;

    @NotNull(message = "Ngày xuất bản không được bỏ trống")
    private Date publishedDate;

    private String language;
}

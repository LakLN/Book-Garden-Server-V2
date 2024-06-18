package com.example.bookgarden.dto;

import jakarta.validation.constraints.NotEmpty;
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
    private String categories;

    @NotEmpty(message = "Tác giả không được bỏ trống")
    private String authors;

    @NotEmpty(message = "Giá tiền không được bỏ trống")
    private String price;

    @NotEmpty(message = "Số lượng không được bỏ trống")
    private String stock;

    @NotEmpty(message = "Mã ISBN không được bỏ trống")
    private String isbn;

    @NotEmpty(message = "Số trang không được bỏ trống")
    private String pageNumbers;

    private int soldQuantity ;

    private String description;

    @NotEmpty(message = "Nhà xuất bản sách không được bỏ trống")
    private String publisher;

    private Date publishedDate;

    private String language;
}

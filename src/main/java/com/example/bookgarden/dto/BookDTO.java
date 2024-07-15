package com.example.bookgarden.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String _id;
    private String title;
    private List<CategoryDTO> categories;
    private List<AuthorDTO> authors;
    private double price;
    private int stock;
    private int soldQuantity;
    private String description;
    private String isbn;
    private String image;
    private boolean isDeleted;
    private String publisher;
    private int discountPercent = 0;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;
    public BookDTO(String _id, String title)
    {
        this._id = _id;
        this.title = title;
    }
}

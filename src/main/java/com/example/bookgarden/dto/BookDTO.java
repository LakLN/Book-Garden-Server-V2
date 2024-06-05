package com.example.bookgarden.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}

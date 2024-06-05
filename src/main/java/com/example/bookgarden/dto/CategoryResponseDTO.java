package com.example.bookgarden.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryResponseDTO {
    private String id;
    private String categoryName;
    private List<BookDTO> books;
}

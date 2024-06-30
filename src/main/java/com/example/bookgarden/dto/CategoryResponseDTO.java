package com.example.bookgarden.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private String id;
    private String categoryName;
    private List<BookDTO> books;
}

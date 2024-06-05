package com.example.bookgarden.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthorResponseDTO {
    private String id;
    private String authorName;
    private List<BookDTO> books;
}
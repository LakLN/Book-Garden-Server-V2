package com.example.bookgarden.dto;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SearchHistoryRequestDTO {
    @NotEmpty(message = "Search query cannot be empty")
    private String searchQuery;
}
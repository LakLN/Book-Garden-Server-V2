package com.example.bookgarden.dto;

import lombok.Data;

@Data
public class ReviewBookRequestDTO {
    private String review;
    private int rating;
}

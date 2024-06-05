package com.example.bookgarden.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ReviewDTO {
    private String id;
    private UserPostDTO user;
    private String review;
    private int rating;
    private Date createdAt;
}

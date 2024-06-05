package com.example.bookgarden.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class PostCreateRequestDTO {
    private String title;
    private String content;
    private String bookId;
}

package com.example.bookgarden.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PostResponseDTO {
    private String id;
    private String title;
    private String content;
    private String status;
    private UserPostDTO postedBy;
    private Date postedDate;
    private BookPostDTO book;
    private List<CommentDTO> comments;
}

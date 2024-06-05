package com.example.bookgarden.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CommentDTO {
    private String id;
    private String post;
    private UserPostDTO user;
    private String comment;
    private Date createdAt = new Date();
    private List<CommentDTO> replies = new ArrayList<>();
}

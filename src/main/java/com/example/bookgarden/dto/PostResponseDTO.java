package com.example.bookgarden.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;

@Data
public class PostResponseDTO {
    private String id;
    private String title;
    private String content;
    private String status;
    private UserPostDTO postedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date postedDate;
    private BookPostDTO book;
    private List<CommentDTO> comments;
}

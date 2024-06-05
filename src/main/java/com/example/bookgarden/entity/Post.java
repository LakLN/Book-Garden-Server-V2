package com.example.bookgarden.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {
    @Id
    private ObjectId id;
    private String title;
    private String content;
    private ObjectId postedBy;
    private Date postedDate = new Date();
    private String status;
    private ObjectId book;
    private List<ObjectId> comments;
}

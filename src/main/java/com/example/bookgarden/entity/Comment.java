package com.example.bookgarden.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {
    @Id
    private ObjectId id;
    private ObjectId user;
    private ObjectId post;
    private String comment;
    private Date createdDate = new Date();
    private List<ObjectId> replies = new ArrayList<>();
}

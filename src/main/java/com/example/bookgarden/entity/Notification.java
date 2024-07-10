package com.example.bookgarden.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification implements Serializable {
    @Id
    private ObjectId id;
    private String userId;
    private String title;
    private String message;
    private Date createdAt;
    private boolean read;
}

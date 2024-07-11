package com.example.bookgarden.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String url;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date createdAt = new Date();
    private boolean read = false;
}

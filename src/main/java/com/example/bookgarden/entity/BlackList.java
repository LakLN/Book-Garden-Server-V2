package com.example.bookgarden.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "blacklists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlackList {
    @Id
    private ObjectId id;
    private ObjectId userId;

    private String reason;

    private Date createdAt = new Date();
}

package com.example.bookgarden.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author implements Serializable {
    @Id
    private ObjectId id;

    private String authorName;

    private List<ObjectId> books;
    private Boolean isDeleted = false;
    public Author(String authorName) {
        this.authorName = authorName;
        this.books = new ArrayList<>();
    }
}

package com.example.bookgarden.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Serializable {
    @Id
    private ObjectId id;

    private String title;

    private List<ObjectId> categories;

    private List<ObjectId> authors;

    private double price;

    private int stock;

    private int soldQuantity = 0;

    private boolean isDeleted = false;

    private List<ObjectId> reviews;
}

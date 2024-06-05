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

@Document(collection = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishList implements Serializable {
    @Id
    private ObjectId id;
    private ObjectId user;
    private List<ObjectId> books;

    public WishList(ObjectId user) {
        this.user = user;
        this.books = new ArrayList<>();
    }
}

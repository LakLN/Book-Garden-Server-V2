package com.example.bookgarden.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {
    @Id
    private ObjectId id;

    private ObjectId user;

    private List<ObjectId> items;

    public Cart(ObjectId user) {
        this.user = user;
        this.items = new ArrayList<>();
    }
}

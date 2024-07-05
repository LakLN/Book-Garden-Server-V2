package com.example.bookgarden.entity;

import org.bson.types.ObjectId;

import javax.persistence.Id;
import java.io.Serializable;

public class Rating implements Serializable {
    @Id
    private ObjectId id;
    private ObjectId userId;
    private ObjectId bookId;
    private int rating;
}
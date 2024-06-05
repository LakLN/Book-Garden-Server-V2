package com.example.bookgarden.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "bookdetails")
@Getter
@Setter
@NoArgsConstructor
public class BookDetail implements Serializable {
    @Id
    private ObjectId id;

    private ObjectId book;
    private int pageNumbers;
    private String publisher;
    private Date publishedDate;
    private String language;
    private String isbn;
    private String description;
    private String image = "https://res.cloudinary.com/dfwwu6ft4/image/upload/v1702465899/book-default_jzwdlj.jpg";
}

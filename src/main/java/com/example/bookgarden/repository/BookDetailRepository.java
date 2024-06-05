package com.example.bookgarden.repository;

import com.example.bookgarden.entity.BookDetail;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookDetailRepository extends MongoRepository<BookDetail, ObjectId> {
    Optional<BookDetail> findByBook(ObjectId book);

    List<BookDetail> findAll();

}



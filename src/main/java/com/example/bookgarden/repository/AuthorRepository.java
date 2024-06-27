package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Author;
import com.example.bookgarden.entity.Category;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends MongoRepository<Author, String> {
    @Cacheable("authors")
    List<Author> findAllByIdIn(List<ObjectId> ids);
    Optional<Author> findByAuthorName(String authorName);
    Optional<Author> findById(ObjectId authorId);
}

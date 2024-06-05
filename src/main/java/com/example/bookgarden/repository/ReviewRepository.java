package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Review;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, ObjectId> {
    @Cacheable("reviews")
    List<Review> findAllByIdIn(List<ObjectId> categoryIds);
}

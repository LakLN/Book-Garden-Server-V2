package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Category;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findById(ObjectId objectId);
    Optional<Category> findByCategoryName(String categoryName);
    @Cacheable("categories")
    List<Category> findAllByIdIn(List<ObjectId> categoryIds);
}

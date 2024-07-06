package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Discount;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends MongoRepository<Discount, ObjectId> {
    Optional<Discount> findByBookId(ObjectId objectId);
}

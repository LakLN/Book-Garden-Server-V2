package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Cart;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, ObjectId> {
    Optional<Cart> findById (ObjectId objectId);
    Optional<Cart> findByUser(ObjectId userId);

}

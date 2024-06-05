package com.example.bookgarden.repository;

import com.example.bookgarden.entity.CartItem;
import com.example.bookgarden.entity.OrderItem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends MongoRepository<CartItem, ObjectId> {
    Optional<CartItem> findById(ObjectId objectId);
    Optional<CartItem> findByBook(ObjectId objectId);
    List<CartItem> findAllByIdIn(List<ObjectId> objectIds);

}

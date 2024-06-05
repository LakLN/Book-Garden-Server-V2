package com.example.bookgarden.repository;

import com.example.bookgarden.entity.OrderItem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends MongoRepository<OrderItem, ObjectId> {
    List<OrderItem> findByIdIn(List<ObjectId> orderItemIds);


}

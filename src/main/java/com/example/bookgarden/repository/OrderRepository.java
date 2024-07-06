package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, ObjectId> {
    @Override
    Optional<Order> findById(ObjectId objectId);
    List<Order> findByUser(ObjectId objectId);
    List<Order> findByPaymentStatus(String paymentStatus);
    List<Order> findTop10ByOrderByOrderDateDesc();

}

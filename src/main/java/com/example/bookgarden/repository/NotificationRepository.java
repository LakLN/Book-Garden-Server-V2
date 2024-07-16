package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Notification;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, ObjectId> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByCreatedBy(String createdBy);

}

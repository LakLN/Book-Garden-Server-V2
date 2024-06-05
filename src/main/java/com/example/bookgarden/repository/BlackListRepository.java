package com.example.bookgarden.repository;

import com.example.bookgarden.entity.BlackList;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlackListRepository extends MongoRepository<BlackList, String> {
    Optional<BlackList> findByUserId(ObjectId userId);
}
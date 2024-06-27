package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Cart;
import com.example.bookgarden.entity.WishList;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishListRepository extends MongoRepository<WishList, ObjectId> {
    Optional<WishList> findById (ObjectId objectId);
    Optional<WishList> findByUser(ObjectId objectId);
}

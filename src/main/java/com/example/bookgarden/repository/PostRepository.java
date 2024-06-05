package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Post;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, ObjectId> {
    List<Post> findAllByStatus(String status, Sort sort);
    List<Post> findAllByPostedBy(ObjectId userId, Sort sort);
}

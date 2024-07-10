package com.example.bookgarden.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.bookgarden.entity.User;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmailAndIsActiveIsTrue(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndIsActiveIsTrue(String id);
    Optional<User> findById(String Id);
    List<User> findByIsVerifiedFalseAndCreatedAtBefore(Date date);
    List<User> findByIsActiveTrue();
    @Query(value = "{ 'role': 'Customer' }", fields = "{ '_id': 1 }")
    List<User> findAllCustomerUsers();
}
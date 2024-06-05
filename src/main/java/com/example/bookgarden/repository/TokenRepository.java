package com.example.bookgarden.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.bookgarden.entity.Token;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token,String> {
    Optional<Token> findByUserId(String userId);
    Optional<Token> findByToken(String refreshToken);
    List<Token> findByUserIdOrderByCreatedAtDesc (String userId);
}

package com.example.bookgarden.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.bookgarden.entity.OTP;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPRepository extends MongoRepository<OTP, String> {
    List<OTP> findByExpirationTimeBefore(LocalDateTime expirationTime);
    Optional<OTP> findByEmail(String email);
}

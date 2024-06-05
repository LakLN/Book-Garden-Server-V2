package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    Optional<Address> findByAddress(String address);
    Optional<Address> findById(String id);
}

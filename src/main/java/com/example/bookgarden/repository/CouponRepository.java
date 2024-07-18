package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Coupon;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends MongoRepository<Coupon, ObjectId> {
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByStartDateBeforeAndEndDateAfterAndQuantityGreaterThanAndIsActive(Date startDate, Date endDate, int quantity, boolean isActive);

}
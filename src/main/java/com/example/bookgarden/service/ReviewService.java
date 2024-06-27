package com.example.bookgarden.service;

import com.example.bookgarden.entity.Author;
import com.example.bookgarden.entity.Review;
import com.example.bookgarden.repository.AuthorRepository;
import com.example.bookgarden.repository.ReviewRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

}

package com.example.bookgarden.repository;

import com.example.bookgarden.entity.SearchHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SearchHistoryRepository extends MongoRepository<SearchHistory, String> {
    List<SearchHistory> findByUserId(String userId);

    SearchHistory findByUserIdAndSearchQuery(String userId, String searchQuery);
}

package com.example.bookgarden.service;

import com.example.bookgarden.entity.SearchHistory;
import com.example.bookgarden.exception.ItemNotFoundException;
import com.example.bookgarden.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    @CacheEvict(value = "searchHistoryCache", key = "#userId")
    public SearchHistory saveSearchHistory(String userId, String searchQuery) {
        Optional<SearchHistory> existingSearchHistory = searchHistoryRepository.findByUserIdAndSearchQuery(userId, searchQuery);
        SearchHistory searchHistory;
        if (existingSearchHistory.isPresent()) {
            searchHistory = existingSearchHistory.get();
            searchHistory.setSearchDate(LocalDateTime.now());
        } else {
            searchHistory = new SearchHistory(userId, searchQuery);
        }
        return searchHistoryRepository.save(searchHistory);
    }
    @Cacheable(value = "searchHistoryCache", key = "#userId")
    public List<String> getSearchHistoryByUserId(String userId) {
        List<SearchHistory> searchHistories = searchHistoryRepository.findByUserIdOrderBySearchDateDesc(userId, PageRequest.of(0, 10));
        return searchHistories.stream()
                .map(SearchHistory::getSearchQuery)
                .collect(Collectors.toList());
    }
    @CacheEvict(value = "searchHistoryCache", key = "#userId")
    public void deleteSearchHistoryItem(String userId, String searchQuery) {
        Optional<SearchHistory> searchHistory = searchHistoryRepository.findByUserIdAndSearchQuery(userId, searchQuery);
        if (searchHistory != null) {
            searchHistoryRepository.delete(searchHistory.get());
        }else {
            throw new ItemNotFoundException("Không tìm thấy mục lịch sử tìm kiếm với query: " + searchQuery);
        }
    }
}

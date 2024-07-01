package com.example.bookgarden.service;

import com.example.bookgarden.entity.SearchHistory;
import com.example.bookgarden.exception.ItemNotFoundException;
import com.example.bookgarden.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    public SearchHistory saveSearchHistory(String userId, String searchQuery) {
        SearchHistory searchHistory = new SearchHistory(userId, searchQuery);
        return searchHistoryRepository.save(searchHistory);
    }
    @Cacheable(value = "searchHistoryCache", key = "#userId")
    public List<String> getSearchHistoryByUserId(String userId) {
        List<SearchHistory> searchHistories = searchHistoryRepository.findByUserId(userId);
        return searchHistories.stream()
                .map(SearchHistory::getSearchQuery)
                .collect(Collectors.toList());
    }
    @CacheEvict(value = "searchHistoryCache", key = "#userId")
    public void deleteSearchHistoryItem(String userId, String searchQuery) {
        SearchHistory searchHistory = searchHistoryRepository.findByUserIdAndSearchQuery(userId, searchQuery);
        if (searchHistory != null) {
            searchHistoryRepository.delete(searchHistory);
        }else {
            throw new ItemNotFoundException("Không tìm thấy mục lịch sử tìm kiếm với query: " + searchQuery);
        }
    }
}

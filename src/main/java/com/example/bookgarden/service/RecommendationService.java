package com.example.bookgarden.service;

import com.example.bookgarden.dto.BookDTO;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.Order;
import com.example.bookgarden.entity.SearchHistory;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.OrderRepository;
import com.example.bookgarden.repository.SearchHistoryRepository;
import com.example.bookgarden.repository.UserRepository;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private BookService bookService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public List<BookDTO> recommendBooks(String userId) {
        List<User> allUsers = userRepository.findAll();

        Map<ObjectId, Map<ObjectId, Double>> userBookMatrix = new HashMap<>();

        for (User user : allUsers) {
            List<Order> userOrders = orderRepository.findByUser(new ObjectId(user.getId()));
            ObjectId userObjectId = new ObjectId(user.getId());
            userBookMatrix.putIfAbsent(userObjectId, new HashMap<>());
            for (Order order : userOrders) {
                for (ObjectId bookId : order.getOrderItems()) {
                    userBookMatrix.get(userObjectId).merge(bookId, 1.0, Double::sum);
                }
            }
        }

        for (User user : allUsers) {
            List<SearchHistory> searchHistories = searchHistoryRepository.findByUserId(user.getId());
            ObjectId userObjectId = new ObjectId(user.getId());
            for (SearchHistory searchHistory : searchHistories) {
                String searchQuery = searchHistory.getSearchQuery();
                List<Book> searchResultBooks = bookRepository.findByTitleContainingIgnoreCase(searchQuery);
                for (Book book : searchResultBooks) {
                    userBookMatrix.get(userObjectId).merge(book.getId(), 1.0, Double::sum);
                }
            }
        }

        Map<ObjectId, Integer> userIndexMap = new HashMap<>();
        Map<ObjectId, Integer> bookIndexMap = new HashMap<>();
        RealMatrix matrix = createRealMatrix(userBookMatrix, userIndexMap, bookIndexMap);

        ObjectId targetUserId = new ObjectId(userId);
        int targetUserIndex = userIndexMap.getOrDefault(targetUserId, -1);

        List<Book> recommendedBooks = applyCollaborativeFiltering(matrix, targetUserIndex, userBookMatrix, bookIndexMap);

        if (recommendedBooks.size() < 10) {
            int remainingCount = 10 - recommendedBooks.size();
            List<Book> additionalBooks = getAdditionalBooks(userId, remainingCount);
            recommendedBooks.addAll(additionalBooks);
        }

        List<BookDTO> bookDTOs = recommendedBooks.stream()
                .map(bookService::convertToBookDTO)
                .collect(Collectors.toList());

        return bookDTOs;
    }

    private RealMatrix createRealMatrix(Map<ObjectId, Map<ObjectId, Double>> userBookMatrix, Map<ObjectId, Integer> userIndexMap, Map<ObjectId, Integer> bookIndexMap) {
        int userCount = userBookMatrix.size();
        Set<ObjectId> bookIdsSet = userBookMatrix.values().stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());
        int bookCount = bookIdsSet.size();

        RealMatrix matrix = new BlockRealMatrix(userCount, bookCount);

        int userIndex = 0;
        for (ObjectId userId : userBookMatrix.keySet()) {
            userIndexMap.put(userId, userIndex++);
        }

        int bookIndex = 0;
        for (ObjectId bookId : bookIdsSet) {
            bookIndexMap.put(bookId, bookIndex++);
        }

        for (Map.Entry<ObjectId, Map<ObjectId, Double>> userEntry : userBookMatrix.entrySet()) {
            int rowIndex = userIndexMap.get(userEntry.getKey());
            for (Map.Entry<ObjectId, Double> bookEntry : userEntry.getValue().entrySet()) {
                int colIndex = bookIndexMap.get(bookEntry.getKey());
                matrix.setEntry(rowIndex, colIndex, bookEntry.getValue());
            }
        }
        return matrix;
    }

    private List<Book> applyCollaborativeFiltering(RealMatrix matrix, int targetUserIndex, Map<ObjectId, Map<ObjectId, Double>> userBookMatrix, Map<ObjectId, Integer> bookIndexMap) {
        PearsonsCorrelation correlation = new PearsonsCorrelation(matrix.transpose());
        RealMatrix similarityMatrix = correlation.getCorrelationMatrix();

        double[] targetUserSimilarities = similarityMatrix.getRow(targetUserIndex);
        List<Integer> similarUserIndices = findTopNSimilarUsers(targetUserSimilarities, 10);

        Map<ObjectId, Double> recommendedBookScores = new HashMap<>();
        for (int similarUserIndex : similarUserIndices) {
            if (similarUserIndex == targetUserIndex) continue;
            for (int col = 0; col < matrix.getColumnDimension(); col++) {
                double value = matrix.getEntry(similarUserIndex, col);
                if (value > 0) {
                    ObjectId bookId = getKeyFromValue(bookIndexMap, col);
                    recommendedBookScores.merge(bookId, value, Double::sum);
                }
            }
        }

        List<Map.Entry<ObjectId, Double>> sortedRecommendations = recommendedBookScores.entrySet()
                .stream()
                .sorted(Map.Entry.<ObjectId, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<Book> recommendedBooks = new ArrayList<>();
        for (Map.Entry<ObjectId, Double> entry : sortedRecommendations) {
            bookRepository.findById(entry.getKey()).ifPresent(recommendedBooks::add);
        }

        return recommendedBooks;
    }

    private List<Integer> findTopNSimilarUsers(double[] similarities, int n) {
        List<Integer> similarUserIndices = new ArrayList<>();
        for (int i = 0; i < similarities.length; i++) {
            similarUserIndices.add(i);
        }
        return similarUserIndices.stream()
                .sorted(Comparator.comparingDouble(i -> -similarities[i]))
                .limit(n)
                .collect(Collectors.toList());
    }

    private <T, E> T getKeyFromValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<Book> getAdditionalBooks(String userId, int limit) {
        List<Book> additionalBooks = new ArrayList<>();
        additionalBooks.addAll(getNewBooks(limit));
        if (additionalBooks.size() < limit) {
            additionalBooks.addAll(getBestsellingBooks(limit - additionalBooks.size()));
        }
//        if (additionalBooks.size() < limit) {
//            additionalBooks.addAll(getTopRatedBooks(limit - additionalBooks.size()));
//        }
        if (additionalBooks.size() < limit) {
            additionalBooks.addAll(getBooksByUserPreferences(userId, limit - additionalBooks.size()));
        }
        return additionalBooks;
    }

    private List<Book> getNewBooks(int limit) {
        List<Order> recentOrders = orderRepository.findTop10ByOrderByOrderDateDesc();
        Set<ObjectId> recentBookIds = recentOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.toSet());
        List<Book> recentBooks = new ArrayList<>();
        for (ObjectId bookId : recentBookIds) {
            bookRepository.findById(bookId).ifPresent(recentBooks::add);
        }
        return recentBooks.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Book> getBestsellingBooks(int limit) {
        return bookRepository.findTop10ByOrderBySoldQuantityDesc().stream().limit(limit).collect(Collectors.toList());
    }

//    private List<Book> getTopRatedBooks(int limit) {
//        return bookRepository.findTop10ByOrderByAverageRatingDesc().stream().limit(limit).collect(Collectors.toList());
//    }

    private List<Book> getBooksByUserPreferences(String userId, int limit) {
        ObjectId userObjectId = new ObjectId(userId);
        List<Order> userOrders = orderRepository.findByUser(userObjectId);
        Set<ObjectId> preferredCategoryIds = new HashSet<>();
        Set<ObjectId> preferredAuthorIds = new HashSet<>();
        for (Order order : userOrders) {
            for (ObjectId bookId : order.getOrderItems()) {
                bookRepository.findById(bookId).ifPresent(book -> {
                    preferredCategoryIds.addAll(book.getCategories());
                    preferredAuthorIds.addAll(book.getAuthors());
                });
            }
        }

        List<Book> preferredBooks = new ArrayList<>();
        for (ObjectId categoryId : preferredCategoryIds) {
            preferredBooks.addAll(bookRepository.findByCategoriesContains(categoryId));
        }
        for (ObjectId authorId : preferredAuthorIds) {
            preferredBooks.addAll(bookRepository.findByAuthorsContains(authorId));
        }
        return preferredBooks.stream().limit(limit).collect(Collectors.toList());
    }
}

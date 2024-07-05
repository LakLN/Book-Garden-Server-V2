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
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

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
        List<Book> recommendedBooks = getCollaborativeFilteringRecommendations(userId);

        // Nếu số lượng sách gợi ý < 10, thêm sách từ các tiêu chí bổ sung
        if (recommendedBooks.size() < 10) {
            recommendedBooks.addAll(getBooksByFavoriteGenres(userId, 10 - recommendedBooks.size()));
        }
        if (recommendedBooks.size() < 10) {
            recommendedBooks.addAll(getBooksByFavoriteAuthors(userId, 10 - recommendedBooks.size()));
        }
        if (recommendedBooks.size() < 10) {
            recommendedBooks.addAll(getNewBooks(10 - recommendedBooks.size()));
        }
        if (recommendedBooks.size() < 10) {
            recommendedBooks.addAll(getBestsellingBooks(10 - recommendedBooks.size()));
        }
        if (recommendedBooks.size() < 10) {
            recommendedBooks.addAll(getTopRatedBooks(10 - recommendedBooks.size()));
        }

        // Chuyển đổi danh sách sách thành DTO
        List<BookDTO> bookDTOs = recommendedBooks.stream()
                .map(bookService::convertToBookDTO)
                .distinct() // Loại bỏ các sách trùng lặp
                .limit(10) // Giới hạn số lượng sách gợi ý là 10
                .collect(Collectors.toList());
        return bookDTOs;
    }

    private List<Book> getCollaborativeFilteringRecommendations(String userId) {
        // Lấy tất cả người dùng
        List<User> allUsers = userRepository.findAll();
        logger.info("Total Users: {}", allUsers.size());

        // Tạo ra tập dữ liệu để sử dụng cho thuật toán Collaborative Filtering
        Map<ObjectId, Map<ObjectId, Double>> userBookMatrix = new HashMap<>();

        // Điền dữ liệu từ lịch sử đặt hàng vào ma trận cho tất cả người dùng
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

        // Điền dữ liệu từ lịch sử tìm kiếm vào ma trận cho tất cả người dùng
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

        // Log ma trận người dùng - sách
        logger.info("User-Book Matrix: {}", userBookMatrix);

        // Kiểm tra dữ liệu đủ trước khi tính toán
        if (userBookMatrix.size() <= 1) {
            throw new IllegalArgumentException("Insufficient data: At least two users are required.");
        }

        // Chuyển đổi userBookMatrix thành ma trận thực
        Map<ObjectId, Integer> userIndexMap = new HashMap<>();
        Map<ObjectId, Integer> bookIndexMap = new HashMap<>();
        RealMatrix matrix = createRealMatrix(userBookMatrix, userIndexMap, bookIndexMap);

        // Log số lượng hàng và cột của ma trận
        logger.info("Matrix dimensions: {} rows, {} columns", matrix.getRowDimension(), matrix.getColumnDimension());

        // Tìm chỉ số người dùng mục tiêu
        ObjectId targetUserId = new ObjectId(userId);
        int targetUserIndex = userIndexMap.getOrDefault(targetUserId, -1);

        if (targetUserIndex == -1) {
            logger.error("Target user ID not found in user-book matrix.");
            throw new IllegalArgumentException("Target user ID not found in user-book matrix.");
        }

        // Áp dụng thuật toán Collaborative Filtering để tìm các sách gợi ý
        List<Book> recommendedBooks = applyCollaborativeFiltering(matrix, targetUserIndex, userBookMatrix, bookIndexMap);
        return recommendedBooks;
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

        logger.info("Matrix created: {} rows, {} columns", matrix.getRowDimension(), matrix.getColumnDimension());
        return matrix;
    }

    private List<Book> applyCollaborativeFiltering(RealMatrix matrix, int targetUserIndex, Map<ObjectId, Map<ObjectId, Double>> userBookMatrix, Map<ObjectId, Integer> bookIndexMap) {
        // Tính toán độ tương đồng giữa các người dùng
        PearsonsCorrelation correlation = new PearsonsCorrelation(matrix.transpose()); // Chuyển vị để tính tương đồng giữa người dùng
        RealMatrix similarityMatrix = correlation.getCorrelationMatrix();
        logger.info("Similarity matrix created: {} rows, {} columns", similarityMatrix.getRowDimension(), similarityMatrix.getColumnDimension());

        // Tìm các sách gợi ý dựa trên độ tương đồng
        double[] targetUserSimilarities = similarityMatrix.getRow(targetUserIndex);
        List<Integer> similarUserIndices = findTopNSimilarUsers(targetUserSimilarities, 10);
        logger.info("Top N similar users: {}", similarUserIndices);

        // Tổng hợp các gợi ý sách từ các người dùng tương tự
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

        // Sắp xếp các sách theo điểm gợi ý
        List<Map.Entry<ObjectId, Double>> sortedRecommendations = recommendedBookScores.entrySet()
                .stream()
                .sorted(Map.Entry.<ObjectId, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Lấy danh sách các sách gợi ý
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

    private List<Book> getBooksByFavoriteGenres(String userId, int limit) {
        // Lấy các thể loại yêu thích của người dùng từ lịch sử đặt hàng và tìm kiếm
        // Gợi ý sách từ các thể loại này
        List<Book> favoriteGenreBooks = new ArrayList<>();
        Set<ObjectId> favoriteGenres = getFavoriteGenres(userId);
        for (ObjectId genreId : favoriteGenres) {
            favoriteGenreBooks.addAll(bookRepository.findByCategoriesContains(genreId));
            if (favoriteGenreBooks.size() >= limit) {
                break;
            }
        }
        return favoriteGenreBooks.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Book> getBooksByFavoriteAuthors(String userId, int limit) {
        // Lấy các tác giả yêu thích của người dùng từ lịch sử đặt hàng và tìm kiếm
        // Gợi ý sách từ các tác giả này
        List<Book> favoriteAuthorBooks = new ArrayList<>();
        Set<ObjectId> favoriteAuthors = getFavoriteAuthors(userId);
        for (ObjectId authorId : favoriteAuthors) {
            favoriteAuthorBooks.addAll(bookRepository.findByAuthorsContains(authorId));
            if (favoriteAuthorBooks.size() >= limit) {
                break;
            }
        }
        return favoriteAuthorBooks.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Book> getNewBooks(int limit) {
        // Gợi ý các sách mới xuất bản
        return bookRepository.findTopByOrderByReleaseDateDesc().stream().limit(limit).collect(Collectors.toList());
    }

    private List<Book> getBestsellingBooks(int limit) {
        // Gợi ý các sách bán chạy
        return bookRepository.findTopByOrderBySoldQuantityDesc().stream().limit(limit).collect(Collectors.toList());
    }

    private List<Book> getTopRatedBooks(int limit) {
        // Gợi ý các sách được đánh giá cao
        return bookRepository.findTopByOrderByAverageRatingDesc().stream().limit(limit).collect(Collectors.toList());
    }

    private Set<ObjectId> getFavoriteGenres(String userId) {
        // Lấy các thể loại yêu thích từ lịch sử đặt hàng và tìm kiếm
        Set<ObjectId> favoriteGenres = new HashSet<>();
        List<Order> userOrders = orderRepository.findByUser(new ObjectId(userId));
        for (Order order : userOrders) {
            for (ObjectId bookId : order.getOrderItems()) {
                Book book = bookRepository.findById(bookId).orElse(null);
                if (book != null) {
                    favoriteGenres.addAll(book.getCategories());
                }
            }
        }
        List<SearchHistory> searchHistories = searchHistoryRepository.findByUserId(userId);
        for (SearchHistory searchHistory : searchHistories) {
            List<Book> searchResultBooks = bookRepository.findByTitleContainingIgnoreCase(searchHistory.getSearchQuery());
            for (Book book : searchResultBooks) {
                favoriteGenres.addAll(book.getCategories());
            }
        }
        return favoriteGenres;
    }

    private Set<ObjectId> getFavoriteAuthors(String userId) {
        // Lấy các tác giả yêu thích từ lịch sử đặt hàng và tìm kiếm
        Set<ObjectId> favoriteAuthors = new HashSet<>();
        List<Order> userOrders = orderRepository.findByUser(new ObjectId(userId));
        for (Order order : userOrders) {
            for (ObjectId bookId : order.getOrderItems()) {
                Book book = bookRepository.findById(bookId).orElse(null);
                if (book != null) {
                    favoriteAuthors.addAll(book.getAuthors());
                }
            }
        }
        List<SearchHistory> searchHistories = searchHistoryRepository.findByUserId(userId);
        for (SearchHistory searchHistory : searchHistories) {
            List<Book> searchResultBooks = bookRepository.findByTitleContainingIgnoreCase(searchHistory.getSearchQuery());
            for (Book book : searchResultBooks) {
                favoriteAuthors.addAll(book.getAuthors());
            }
        }
        return favoriteAuthors;
    }
}

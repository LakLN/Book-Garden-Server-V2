package com.example.bookgarden.service;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.PostResponseDTO;
import com.example.bookgarden.dto.StatisticDTO;
import com.example.bookgarden.entity.Author;
import com.example.bookgarden.entity.Order;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class StatisticService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private OrderRepository orderRepository;

    public ResponseEntity<GenericResponse> getStatistics(String userId) {
        try {
            // Kiểm tra quyền truy cập
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!"Admin".equals(optionalUser.get().getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền truy cập thống kê")
                            .data(null)
                            .build());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }

            List<Order> orders = orderRepository.findByPaymentStatus("PAID");

            StatisticDTO statisticDTO = new StatisticDTO();
            statisticDTO.setBookCount(bookRepository.count());
            statisticDTO.setUserCount(userRepository.count());
            statisticDTO.setPostCount(postRepository.count());
            statisticDTO.setCategoryCount(categoryRepository.count());
            statisticDTO.setAuthorCount(authorRepository.count());
            statisticDTO.setCommentCount(commentRepository.count());
            statisticDTO.setOrderCount(orderRepository.count());

            Map<LocalDate, Double> revenueByDay = new HashMap<>();
            Map<YearMonth, Double> revenueByMonth = new HashMap<>();
            Map<Integer, Double> revenueByYear = new HashMap<>();
            double totalRevenue = 0.0;
            for (Order order : orders) {
                Date orderDate = order.getOrderDate();
                Instant instant = orderDate.toInstant();
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                LocalDate orderLocalDate = localDateTime.toLocalDate();
                YearMonth orderYearMonth = YearMonth.from(localDateTime);
                int orderYear = localDateTime.getYear();

                double orderAmount = order.getTotalAmount();

                double dailyRevenue = revenueByDay.getOrDefault(orderLocalDate, 0.0);
                dailyRevenue += orderAmount;
                revenueByDay.put(orderLocalDate, dailyRevenue);

                double monthlyRevenue = revenueByMonth.getOrDefault(orderYearMonth, 0.0);
                monthlyRevenue += orderAmount;
                revenueByMonth.put(orderYearMonth, monthlyRevenue);

                double yearlyRevenue = revenueByYear.getOrDefault(orderYear, 0.0);
                yearlyRevenue += orderAmount;
                revenueByYear.put(orderYear, yearlyRevenue);

                totalRevenue += orderAmount;
            }
            Map<LocalDate, Double> sortedRevenueByDay = new TreeMap<>(revenueByDay);
            statisticDTO.setRevenueByDay(sortedRevenueByDay);
            statisticDTO.setRevenueByMonth(revenueByMonth);
            statisticDTO.setRevenueByYear(revenueByYear);
            statisticDTO.setTotalRevenue(totalRevenue);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Thông tin thống kê doanh thu")
                    .data(statisticDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin thống kê doanh thu")
                    .data(e.getMessage())
                    .build());
        }
    }
}

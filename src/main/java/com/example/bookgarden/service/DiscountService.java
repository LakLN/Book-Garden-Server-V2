package com.example.bookgarden.service;

import com.example.bookgarden.dto.DiscountDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.entity.Discount;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.exception.AccessDeniedException;
import com.example.bookgarden.exception.NotFoundException;
import com.example.bookgarden.repository.DiscountRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private UserRepository userRepository;
    private void checkManagerAndAdminPermission(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

        if (!"Admin".equals(user.getRole()) && !"Manager".equals(user.getRole())) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện thao tác này");
        }
    }
    public ResponseEntity<GenericResponse> addDiscount(String userId, String bookId, DiscountDTO discountDTO) {
        try {
            checkManagerAndAdminPermission(userId);
            Optional<Discount> optionalDiscount = discountRepository.findByBookId(new ObjectId(bookId));
            Discount discount = optionalDiscount.orElseGet(Discount::new);
            discount.setBookId(new ObjectId(bookId));
            discount.setDiscountPercent(discountDTO.getDiscountPercent());
            discount.setStartDate(discountDTO.getStartDate());
            discount.setEndDate(discountDTO.getEndDate());
            discount = discountRepository.save(discount);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm giảm giá thành công!")
                    .data(discount)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thêm giảm giá")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteDiscount(String userId, String bookId) {
        try {
            checkManagerAndAdminPermission(userId);
            Optional<Discount> optionalDiscount = discountRepository.findByBookId(new ObjectId(bookId));
            if (optionalDiscount.isPresent()){
                discountRepository.deleteById(optionalDiscount.get().getId());
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
//                        .success(false)
//                        .message("Không tìm thấy giảm giá của sách")
//                        .data(null)
//                        .build());
            }
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(false)
                    .message("Xóa giảm giá của sách thành công")
                    .data(null)
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa giảm giá")
                    .data(e.getMessage())
                    .build());
        }
    }
}

package com.example.bookgarden.service;

import com.example.bookgarden.dto.CouponDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.entity.Coupon;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.exception.ForbiddenException;
import com.example.bookgarden.repository.CouponRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<GenericResponse> createCoupon(String userId, CouponDTO couponDTO) {
        try {
            checkAdminAndManagerPermission(userId);
            ModelMapper modelMapper = new ModelMapper();
            Coupon coupon = modelMapper.map(couponDTO, Coupon.class);
            couponRepository.save(coupon);
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .success(true)
                    .message("Thêm mã giảm giá thành công!")
                    .data(coupon)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thêm mã giảm giá")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> updateCoupon(String userId, String couponId, CouponDTO couponDTO) {
        try {
            checkAdminAndManagerPermission(userId);
            Optional<Coupon> optionalCoupon = couponRepository.findById(new ObjectId(couponId));
            if (optionalCoupon.isPresent()) {
                Coupon coupon = optionalCoupon.get();
                coupon.setCode(couponDTO.getCode());
                coupon.setDiscountAmount(couponDTO.getDiscountAmount());
                coupon.setStartDate(couponDTO.getStartDate());
                coupon.setEndDate(couponDTO.getEndDate());
                coupon.setQuantity(couponDTO.getQuantity());
                couponRepository.save(coupon);
                return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật mã giảm giá thành công!")
                        .data(coupon)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy mã giảm giá")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật mã giảm giá")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> getAvailableCoupons() {
        try {
            Date currentDate = new Date();
            List<Coupon> availableCoupons = couponRepository.findByStartDateBeforeAndEndDateAfterAndQuantityGreaterThanAndIsActive(currentDate, currentDate, 0, true);
            List<CouponDTO> couponResponseDTOS = availableCoupons.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách mã giảm giá thành công")
                    .data(couponResponseDTOS)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách mã giảm giá")
                    .data(e.getMessage())
                    .build());
        }
    }
    private void checkAdminAndManagerPermission(String userId) throws ForbiddenException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            String role = optionalUser.get().getRole();
            if (!"Admin".equals(role) && !"Manager".equals(role)) {
                throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này");
            }
        } else {
            throw new ForbiddenException("Người dùng không tồn tại");
        }
    }
    private CouponDTO convertToResponseDTO(Coupon coupon) {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(coupon, CouponDTO.class);
    }
}

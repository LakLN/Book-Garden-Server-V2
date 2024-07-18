package com.example.bookgarden.controller;

import com.example.bookgarden.dto.CouponDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {
    @Autowired
    private CouponService couponService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createCoupon(@RequestHeader("Authorization") String authorizationHeader,
                                                        @RequestBody CouponDTO couponDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return couponService.createCoupon(userId, couponDTO);
    }
    @PutMapping("/{couponId}")
    public ResponseEntity<GenericResponse> updateCoupon(@RequestHeader("Authorization") String authorizationHeader,
                                                        @PathVariable String couponId,
                                                        @RequestBody CouponDTO couponDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return couponService.updateCoupon(userId, couponId, couponDTO);
    }
    @GetMapping("/available")
    public ResponseEntity<GenericResponse> getAvailableCoupons() {
        return couponService.getAvailableCoupons();
    }
}

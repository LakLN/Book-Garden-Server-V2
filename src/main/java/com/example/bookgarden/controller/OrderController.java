package com.example.bookgarden.controller;

import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.UpdateOrderStatusRequestDTO;
import com.example.bookgarden.repository.OrderRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @GetMapping
    public ResponseEntity<GenericResponse> getAllOrders(@RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.getAllOrders(userId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GenericResponse> getOrderById(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String orderId){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.getOrderById(userId, orderId);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<GenericResponse> updateOrderStatus(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String orderId,
                                                             @RequestBody UpdateOrderStatusRequestDTO updateOrderStatusRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.updateOrderStatus(userId, orderId, updateOrderStatusRequestDTO);
    }
}

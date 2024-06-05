package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishListService wishListService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PostService postService;
    @Autowired
    private BookService bookService;

    //Get cart
    @GetMapping("/cart")
    public ResponseEntity<GenericResponse> getCart(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return cartService.getCart(userId);
    }

    //Get wishlist
    @GetMapping("/wishList")
    public ResponseEntity<GenericResponse> getWishList(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return wishListService.getWishList(userId);
    }

    @DeleteMapping("/wishlist/{bookId}")
    public ResponseEntity<GenericResponse> removeFromWishList(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String bookId){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return wishListService.removeFromWishList(userId, bookId);
    }

    //Update cart item
    @PutMapping("/cart/updateCartItem")
    public ResponseEntity<GenericResponse> updateCartItem(@RequestHeader("Authorization") String authorizationHeader, @RequestBody AddToCartRequestDTO addToCartRequestDTO ){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return cartService.updateCartItem(userId, addToCartRequestDTO);
    }

    //Remove cart item
    @DeleteMapping("cart/removeCartItem")
    public ResponseEntity<GenericResponse> removeCartItem(@RequestHeader("Authorization") String authorizationHeader, @RequestBody RemoveCartItemRequestDTO removeCartItemRequestDTO ){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        String cartItemId = removeCartItemRequestDTO.getCartItemID();
        return cartService.removeCartItem(userId, cartItemId);
    }

    //Create Order
    @PostMapping("/orders/create")
    public ResponseEntity<GenericResponse> createOrders(@RequestHeader("Authorization") String authorizationHeader, @RequestBody CreateOrderRequestDTO createOrderRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.createOrder(userId, createOrderRequestDTO);
    }

    //Get history orders
    @GetMapping("/orders")
    public ResponseEntity<GenericResponse> getUserOrders(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.getUserOrders(userId);
    }
    @GetMapping("/posts")
    public ResponseEntity<GenericResponse> getUserPosts(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return postService.getUserPosts(userId);
    }

    @PostMapping("/{bookId}/review")
    public ResponseEntity<GenericResponse> reviewBook(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String bookId,
                                                      @RequestBody ReviewBookRequestDTO reviewBookRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookService.reviewBook(userId, bookId, reviewBookRequestDTO);
    }
}

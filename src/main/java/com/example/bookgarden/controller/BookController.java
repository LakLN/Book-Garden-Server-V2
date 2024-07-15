package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Discount;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.BookService;
import com.example.bookgarden.service.CartService;
import com.example.bookgarden.service.DiscountService;
import com.example.bookgarden.service.WishListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    @Autowired
    private BookService bookService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishListService wishListService;
    @Autowired
    private DiscountService discountService;

    // Get all books
    @GetMapping("")
    public ResponseEntity<?> getAllBooks (){
        return bookService.getAllBooks();
    }

    //Get all deleted books
    @GetMapping("/deleted")
    public ResponseEntity<?> getAllDeletedBooks (@RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookService.getAllDeletedBooks(userId);
    }

    // Get one book
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookDetailById(@PathVariable String bookId) {
        return bookService.getBookById(bookId);
    }

    // Get list related books
    @GetMapping("/{bookId}/related")
    public ResponseEntity<GenericResponse> getRelatedBooks(@PathVariable String bookId) {
        return bookService.getRelatedBooks(bookId);
    }

    // Get Best seller books
    @GetMapping("/best-seller")
    public ResponseEntity<GenericResponse> getBestSellerBooks(){
        return bookService.getBestSellerBooks();
    }
    //Add book
    @PostMapping("/add")
    public ResponseEntity<?> addBook(@RequestHeader("Authorization") String authorizationHeader, @Valid @ModelAttribute AddBookRequestDTO addBookRequestDTO,
                                     MultipartHttpServletRequest imageRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : errors) {
                String errorMessage = error.getDefaultMessage();
                errorMessages.add(errorMessage);
            }
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookService.addBook(userId, addBookRequestDTO, imageRequest);
    }
    //Update book
    @PutMapping("/{bookId}")
    public ResponseEntity<GenericResponse> updateBook(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String bookId,
                                                      @Valid @ModelAttribute UpdateBookRequestDTO updateBookRequestDTO, MultipartHttpServletRequest imageRequest,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : errors) {
                String errorMessage = error.getDefaultMessage();
                errorMessages.add(errorMessage);
            }
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookService.updateBook(userId, bookId, updateBookRequestDTO, imageRequest);
    }
    //Delete Book
    @DeleteMapping("/{bookId}")
    public ResponseEntity<GenericResponse> deteleBook(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String bookId){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookService.deleteBook(userId, bookId);
    }
    // Restore book
    @PutMapping("/{bookId}/restore")
    public ResponseEntity<GenericResponse> restoreBook(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String bookId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return bookService.restoreBook(userId, bookId);
    }

    //Add to cart
    @PostMapping ("/{bookId}/addToCart")
    public ResponseEntity<GenericResponse> addToCart(@RequestHeader("Authorization") String authorizationHeader, @RequestBody AddToCartRequestDTO addToCartRequestDTO){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return cartService.addToCart(userId, addToCartRequestDTO);
    }

    //Add to wishlist
    @PostMapping ("/{bookId}/addToWishList")
    public ResponseEntity<GenericResponse> addToWishList(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String bookId){
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return wishListService.addToWishList(userId, bookId);
    }

    //Add Discount
    @PostMapping("/{bookId}/discounts")
    public ResponseEntity<GenericResponse> addDiscounts(@RequestHeader("Authorization") String authorizationHeader,
                                                        @PathVariable String bookId,
                                                        @Valid @RequestBody DiscountDTO discountDTO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<String> errorMessages = new ArrayList<>();
            for (ObjectError error : errors) {
                String errorMessage = error.getDefaultMessage();
                errorMessages.add(errorMessage);
            }
            return ResponseEntity.status(400).body(GenericResponse.builder()
                    .success(false)
                    .message("Dữ liệu đầu vào không hợp lệ")
                    .data(errorMessages)
                    .build());
        }
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return discountService.addDiscount(userId, bookId, discountDTO);
    }
    //Delete Discount
    @DeleteMapping("/{bookId}/discounts")
    public ResponseEntity<GenericResponse> deleteDiscounts(@RequestHeader("Authorization") String authorizationHeader,
                                                        @PathVariable String bookId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return discountService.deleteDiscount(userId, bookId);
    }
    //Get All Discount
    @GetMapping("/discounts")
    public ResponseEntity<GenericResponse> getAllDiscountedBooks() {
        return discountService.getAllDiscountedBooks();
    }
}


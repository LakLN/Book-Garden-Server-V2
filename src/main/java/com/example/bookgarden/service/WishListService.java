package com.example.bookgarden.service;

import com.example.bookgarden.dto.BookDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.entity.WishList;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.repository.WishListRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WishListService {
    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<GenericResponse> addToWishList(String userId, String bookId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }

            ObjectId bookObjectId;
            try {
                bookObjectId = new ObjectId(bookId);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("Book ID không hợp lệ")
                        .data(null)
                        .build());
            }

            Optional<Book> optionalBook = bookRepository.findById(bookObjectId);
            if (optionalBook.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách")
                        .data(null)
                        .build());
            }

            WishList wishList = wishListRepository.findByUser(new ObjectId(userId))
                    .orElseGet(() -> {
                        WishList newWishList = new WishList();
                        newWishList.setUser(new ObjectId(userId));
                        return wishListRepository.save(newWishList);
                    });

            if (wishList.getBooks().contains(bookObjectId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("Sách đã tồn tại trong danh sách yêu thích")
                        .data(null)
                        .build());
            }

            wishList.getBooks().add(bookObjectId);
            wishListRepository.save(wishList);

            List<BookDTO> wishlistResponse = wishList.getBooks().stream()
                    .map(bookItemId -> {
                        Optional<Book> bookOptional = bookRepository.findById(bookItemId);
                        return bookOptional.map(bookService::convertToBookDTO).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Đã thêm sách vào danh sách mong muốn thành công")
                    .data(wishlistResponse)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thêm sách vào danh sách mong muốn")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getWishList(String userId) {
        try {
            Optional<WishList> optionalWishList = getOrCreateWishList(userId);
            if (optionalWishList.isPresent()) {
                WishList wishList = optionalWishList.get();
                List<BookDTO> wishlistResponse = wishList.getBooks().stream()
                        .map(bookItemId -> {
                            Optional<Book> optionalBook = bookRepository.findById(bookItemId);
                            return optionalBook.map(bookService::convertToBookDTO).orElse(null);
                        })
                        .filter(bookDTO -> bookDTO != null)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Lấy thông tin danh sách mong muốn thành công")
                        .data(wishlistResponse)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy danh sách mong muốn của user")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin danh sách mong muốn")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> removeFromWishList(String userId, String bookId) {
        try {
            Optional<WishList> optionalWishList = getOrCreateWishList(userId);
            if (optionalWishList.isPresent()) {
                WishList wishList = optionalWishList.get();
                Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));
                if (optionalBook.isEmpty()){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                            .success(false)
                            .message("Không tìm thấy sách")
                            .data(null)
                            .build());
                }
                Book book = optionalBook.get();
                wishList.getBooks().remove(book.getId());

                wishListRepository.save(wishList);

                List<BookDTO> wishlistResponse = wishList.getBooks().stream()
                        .map(bookItemId -> {
                            Optional<Book> optionalBookItem = bookRepository.findById(bookItemId);
                            return optionalBookItem.map(bookService::convertToBookDTO).orElse(null);
                        })
                        .filter(bookDTO -> bookDTO != null)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Lấy thông tin danh sách mong muốn thành công")
                        .data(wishlistResponse)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy danh sách mong muốn của user")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin danh sách mong muốn")
                    .data(e.getMessage())
                    .build());
        }
    }

    private Optional<WishList> getOrCreateWishList(String userId) {
        Optional<WishList> optionalWishList;

        Optional<WishList> optionalExistingWishList = wishListRepository.findByUser(new ObjectId(userId));
        if (optionalExistingWishList.isEmpty()) {
            WishList newWishList = new WishList(new ObjectId(userId));
            WishList createdWishList = wishListRepository.save(newWishList);
            User user = userRepository.findById(userId).get();
            user.setWishList(createdWishList.getId().toString());
            userRepository.save(user);

            optionalWishList = Optional.of(createdWishList);
        } else {
            optionalWishList = optionalExistingWishList;
        }

        return optionalWishList;
    }
}

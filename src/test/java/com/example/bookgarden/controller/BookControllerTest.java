package com.example.bookgarden.controller;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.Cart;
import com.example.bookgarden.entity.CartItem;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.CartItemRepository;
import com.example.bookgarden.repository.CartRepository;
import com.example.bookgarden.repository.UserRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.BookService;
import com.example.bookgarden.service.WishListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.hamcrest.Matchers.is;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookRepository bookRepository;
    @MockBean
    private CartRepository cartRepository;
    @MockBean
    private CartItemRepository cartItemRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private WishListService wishListService;
    private static final Logger logger = LoggerFactory.getLogger(BookControllerTest.class);

    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetBookById_Success() throws Exception {
        String bookId = new ObjectId().toHexString();
        BookDetailDTO bookDetailDTO = new BookDetailDTO();
        bookDetailDTO.set_id(bookId);
        bookDetailDTO.setTitle("Sample Book");

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy chi tiết sách thành công!")
                .data(bookDetailDTO)
                .build();

        when(bookService.getBookById(bookId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    void testGetBookById_NotFound() throws Exception {
        String bookId = new ObjectId().toHexString();

        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Không tìm thấy sách!")
                .data(null)
                .build();

        when(bookService.getBookById(bookId)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));

        mockMvc.perform(get("/api/v1/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    void testGetRelatedBooks_Success() throws Exception {
        String bookId = new ObjectId().toHexString();
        List<BookDTO> relatedBooks = Collections.singletonList(new BookDTO());

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy danh sách sách liên quan thành công")
                .data(relatedBooks)
                .build();

        when(bookService.getRelatedBooks(bookId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/books/{bookId}/related", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

    @Test
    void testGetBestSellerBooks_Success() throws Exception {
        List<BookDTO> bestSellerBooks = Collections.singletonList(new BookDTO());

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Lấy danh sách sách bán chạy")
                .data(bestSellerBooks)
                .build();

        when(bookService.getBestSellerBooks()).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/v1/books/best-seller")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }

//    @Test
//    void testAddBook_Success() throws Exception {
//        String token = "Bearer mockToken";
//        String userId = "userId1";
//        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test image".getBytes());
//
//        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO();
//        addBookRequestDTO.setTitle("New Book");
//        addBookRequestDTO.setIsbn("1234567890");
//        addBookRequestDTO.setCategories("[\"category1\"]");
//
//        GenericResponse response = GenericResponse.builder()
//                .success(true)
//                .message("Thêm sách thành công")
//                .data(new BookDetailDTO())
//                .build();
//
//        when(bookService.addBook(anyString(), any(AddBookRequestDTO.class), any())).thenReturn(ResponseEntity.ok(response));
//        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn(userId);
//
//        mockMvc.perform(multipart("/api/v1/books/add")
//                        .file(image)
//                        .header("Authorization", token)
//                        .param("title", "New Book")
//                        .param("isbn", "1234567890")
//                        .param("categories", "[\"category1\"]")
//                        .param("authors", "[\"author1\"]")
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isOk())
//                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)))
//                .andDo(result -> {
//                    // Print the response body to see the error details
//                    System.out.println(result.getResponse().getContentAsString());
//                });
//    }

    @Test
    void testDeleteBook_Success() throws Exception {
        String token = "Bearer mockToken";
        String bookId = new ObjectId().toHexString();
        String userId = "userId1";

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Xóa sách thành công")
                .data(null)
                .build();

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn(userId);
        when(bookService.deleteBook(userId, bookId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(delete("/api/v1/books/{bookId}", bookId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(response)));
    }
    @Test
    void testAddToWishList_Success() throws Exception {
        String token = "mock-token";
        String userId = "mock-user-id";
        String bookId = new ObjectId().toHexString(); // valid ObjectId

        when(jwtTokenProvider.getUserIdFromJwt(token)).thenReturn(userId);

        GenericResponse response = GenericResponse.builder()
                .success(true)
                .message("Đã thêm sách vào danh sách mong muốn thành công")
                .data(Collections.emptyList())
                .build();

        when(wishListService.addToWishList(userId, bookId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/v1/books/{bookId}/addToWishList", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void testAddToWishList_BookAlreadyInWishList() throws Exception {
        String token = "mock-token";
        String userId = "mock-user-id";
        String bookId = new ObjectId().toHexString(); // valid ObjectId

        when(jwtTokenProvider.getUserIdFromJwt(token)).thenReturn(userId);

        GenericResponse response = GenericResponse.builder()
                .success(false)
                .message("Sách đã tồn tại trong danh sách yêu thích")
                .data(null)
                .build();

        when(wishListService.addToWishList(userId, bookId)).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));

        mockMvc.perform(post("/api/v1/books/{bookId}/addToWishList", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
}
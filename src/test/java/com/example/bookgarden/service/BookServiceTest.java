package com.example.bookgarden.service;

import com.cloudinary.Cloudinary;
import com.example.bookgarden.dto.BookDTO;
import com.example.bookgarden.dto.BookDetailDTO;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.entity.*;
import com.example.bookgarden.repository.*;
import com.example.bookgarden.security.JwtTokenProvider;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private BookDetailRepository bookDetailRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private AuthorRepository authorRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private ReviewRepository reviewRepository;
    @MockBean
    private Cloudinary cloudinary;
    @Test
    void testGetAllBooks_Success() {
        List<Book> books = Arrays.asList(
                new Book(new ObjectId(), "Book 1", new ArrayList<>(), new ArrayList<>(), 100, 10, 5, false, new ArrayList<>()),
                new Book(new ObjectId(), "Book 2", new ArrayList<>(), new ArrayList<>(), 200, 20, 15, false, new ArrayList<>())
        );

        when(bookRepository.findByIsDeletedFalse()).thenReturn(books);

        ResponseEntity<GenericResponse> response = bookService.getAllBooks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Lấy danh sách sách thành công!", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(2, ((List<BookDTO>) response.getBody().getData()).size());
    }
    @Test
    void testGetAllBooks_Exception() {
        when(bookRepository.findByIsDeletedFalse()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<GenericResponse> response = bookService.getAllBooks();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Lỗi khi lấy danh sách sách", response.getBody().getMessage());
        assertEquals("Database error", response.getBody().getData());
    }
    @Test
    public void testGetAllDeletedBooks_Success() {
        // Mocking
        String userId = "testUserId";
        User user = new User();
        user.setId(userId);
        user.setRole("Admin");

        Book book = new Book();
        book.setId(new ObjectId("60d5f484a1d28b3d94d1e32b"));
        book.setDeleted(true);

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findByIsDeletedTrue()).thenReturn(List.of(book));

        // Call the service
        ResponseEntity<GenericResponse> response = bookService.getAllDeletedBooks(userId);

        // Validate
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Lấy danh sách sách bị xóa thành công!", response.getBody().getMessage());
        assertEquals(1, ((List<BookDTO>) response.getBody().getData()).size());
    }
    @Test
    void testGetBookById_Success() {
        // Arrange
        String bookId = new ObjectId().toHexString(); // Generate a valid ObjectId string
        Book book = new Book();
        book.setId(new ObjectId(bookId));
        book.setTitle("Sample Book");
        book.setCategories(Collections.singletonList(new ObjectId()));
        book.setAuthors(Collections.singletonList(new ObjectId()));
        book.setReviews(Collections.singletonList(new ObjectId()));

        BookDetail bookDetail = new BookDetail();
        bookDetail.setBook(book.getId());
        bookDetail.setDescription("A great book");

        when(bookRepository.findById(new ObjectId(bookId))).thenReturn(Optional.of(book));
        when(bookDetailRepository.findByBook(new ObjectId(bookId))).thenReturn(Optional.of(bookDetail));
        when(categoryRepository.findAllByIdIn(anyList())).thenReturn(Collections.emptyList());
        when(authorRepository.findAllByIdInAndIsDeletedFalse(anyList())).thenReturn(Collections.emptyList());
        when(reviewRepository.findAllByIdIn(anyList())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<GenericResponse> response = bookService.getBookById(bookId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.isSuccess());
        assertEquals("Lấy chi tiết sách thành công!", responseBody.getMessage());
        BookDetailDTO bookDetailDTO = (BookDetailDTO) responseBody.getData();
        assertNotNull(bookDetailDTO);
        assertEquals(bookId, bookDetailDTO.get_id());
        assertEquals("A great book", bookDetailDTO.getDescription());
    }
    @Test
    public void testGetBookById_NotFound() {
        // Mocking
        String bookId = new ObjectId().toString();

        when(bookRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        // Call the service
        ResponseEntity<GenericResponse> response = bookService.getBookById(bookId);

        // Validate
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("Không tìm thấy sách!", response.getBody().getMessage());
    }
    @Test
    void testGetRelatedBooks_Success() {
        ObjectId bookId = new ObjectId();
        Book book = new Book();
        book.setId(bookId);
        book.setAuthors(Arrays.asList(new ObjectId()));
        book.setCategories(Arrays.asList(new ObjectId()));

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.findRelatedBooksByAuthorsAndCategories(any(), any())).thenReturn(Arrays.asList(book));
        when(categoryRepository.findAllByIdIn(any())).thenReturn(Arrays.asList(new Category()));
        when(authorRepository.findAllByIdInAndIsDeletedFalse(any())).thenReturn(Arrays.asList(new Author()));

        ResponseEntity<GenericResponse> response = bookService.getRelatedBooks(bookId.toHexString());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(true, response.getBody().isSuccess());
        assertEquals("Lấy danh sách sách liên quan thành công", response.getBody().getMessage());
    }
    @Test
    public void testGetBestSellerBooks_Success() {
        Book book1 = new Book();
        book1.setId(new ObjectId());
        book1.setTitle("Sample Book");
        book1.setCategories(Collections.singletonList(new ObjectId()));
        book1.setAuthors(Collections.singletonList(new ObjectId()));
        book1.setReviews(Collections.singletonList(new ObjectId()));
        Book book2 = new Book();
        book2.setId(new ObjectId());
        book2.setTitle("Sample Book 2");
        book2.setCategories(Collections.singletonList(new ObjectId()));
        book2.setAuthors(Collections.singletonList(new ObjectId()));
        book2.setReviews(Collections.singletonList(new ObjectId()));
        List<Book> bestSellerBooks = Arrays.asList(book1, book2);
        when(bookRepository.findTop10BySoldQuantityIsNotNullOrderBySoldQuantityDesc()).thenReturn(bestSellerBooks);

        ResponseEntity<GenericResponse> response = bookService.getBestSellerBooks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, ((List<?>) response.getBody().getData()).size());
        assertEquals("Lấy danh sách sách bán chạy thành công!", response.getBody().getMessage());
    }
//    @Test
//    public void testAddBook_Success() throws Exception {
//        String userId = "validUserId";
//        User user = new User();
//        user.setRole("Admin");
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        AddBookRequestDTO addBookRequestDTO = new AddBookRequestDTO();
//
//        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test image".getBytes());
//
//        MockMultipartHttpServletRequest imageRequest = new MockMultipartHttpServletRequest();
//        imageRequest.addFile(image);
//
//        when(cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap())).thenReturn(Map.of("secure_url", "test_url"));
//
//        when(bookRepository.save(any(Book.class))).thenReturn(new Book());
//        when(bookDetailRepository.save(any(BookDetail.class))).thenReturn(new BookDetail());
//
//        ResponseEntity<GenericResponse> response = bookService.addBook(userId, addBookRequestDTO, imageRequest);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertTrue(response.getBody().isSuccess());
//        assertEquals("Thêm sách thành công", response.getBody().getMessage());
//    }
    @Test
    void testDeleteBook_Success() {
        // Arrange
        String bookId = new ObjectId().toHexString();
        String userId = new ObjectId().toHexString();

        Book book = new Book();
        book.setId(new ObjectId(bookId));
        book.setTitle("Sample Book");

        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setRole("Admin");

        when(jwtTokenProvider.getUserIdFromJwt(anyString())).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(bookRepository.findById(new ObjectId(bookId))).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        ResponseEntity<GenericResponse> response = bookService.deleteBook(userId, bookId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        GenericResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.isSuccess());
        assertEquals("Xóa sách thành công", responseBody.getMessage());
        assertNull(responseBody.getData());
    }

}
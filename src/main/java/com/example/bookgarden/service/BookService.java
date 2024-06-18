package com.example.bookgarden.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.*;
import com.example.bookgarden.repository.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookDetailRepository bookDetailRepository;

    @Autowired
    public BookService(BookRepository bookRepository, BookDetailRepository bookDetailRepository) {
        this.bookRepository = bookRepository;
        this.bookDetailRepository = bookDetailRepository;
    }

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    AuthorRepository authorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private  ReviewService reviewService;
    public ResponseEntity<GenericResponse> getAllBooks() {
        try {
            List<Book> books = bookRepository.findByIsDeletedFalse();

            List<BookDTO> bookDTOs = books.stream()
                    .map(this::convertToBookDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách sách thành công")
                    .data(bookDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi lấy sách")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> getAllDeletedBooks(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!"Admin".equals(optionalUser.get().getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền lấy danh sách sách bị xóa")
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

            List<Book> books = bookRepository.findByIsDeletedTrue();

            List<BookDTO> bookDTOs = books.stream()
                    .map(this::convertToBookDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách sách thành công")
                    .data(bookDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi lấy sách")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> deleteDeletedBooks(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!"Admin".equals(optionalUser.get().getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền lấy danh sách sách bị xóa")
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

            long deletedCount = bookRepository.deleteByIsDeletedTrue();

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Đã xóa " + deletedCount + " sách")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa sách")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getBookById (String bookId){
        try {
            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));

            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();
                BookDetailDTO bookDetailDTO = convertToBookDetailDTO(book);
                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Lấy chi tiết sách thành công")
                        .data(bookDetailDTO)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách ")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy chi tiết sách")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> getRelatedBooks(String bookId) {
        try {
            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));

            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();

                List<Book> relatedBooks = findRelatedBooks(book.getAuthors(), book.getCategories());

                List<BookDTO> relatedBookDTOs = relatedBooks.stream()
                        .map(this::convertToBookDTO)
                        .collect(Collectors.toList());

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Lấy danh sách sách liên quan thành công")
                        .data(relatedBookDTOs)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách ")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách sách liên quan")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getBestSellerBooks() {
        try {
            List<Book> books = bookRepository.findTop10BySoldQuantityIsNotNullOrderBySoldQuantityDesc();

            List<BookDTO> bookDTOs = books.stream()
                    .map(this::convertToBookDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách sách bán chạy")
                    .data(bookDTOs)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách sách sách bán chạy")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> addBook(String userId, AddBookRequestDTO addBookRequestDTO, MultipartHttpServletRequest imageRequest){
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()) || "Manager".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền cập nhật sách")
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

            Book book = new Book();
            BookDetail bookDetail = new BookDetail();
            bookDetail.setBook(book.getId());
            bookDetail.setPageNumbers(Integer.parseInt(addBookRequestDTO.getPageNumbers()));
            book.setStock(Integer.parseInt(addBookRequestDTO.getStock()));
            book.setPrice(Double.parseDouble(addBookRequestDTO.getPrice()));
            MultipartFile image = imageRequest.getFile("image");
            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap()).get("secure_url").toString();
                    bookDetail.setImage(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                            .success(false)
                            .message("Lỗi upload ảnh")
                            .data(null)
                            .build());
                }
            }
            ModelMapper modelMapper = new ModelMapper();
            book = modelMapper.map(addBookRequestDTO, Book.class);
            bookDetail = modelMapper.map(addBookRequestDTO, BookDetail.class);
            final Book finalBook = book;

            String updateCategoryies = addBookRequestDTO.getCategories();
            updateCategoryies = StringEscapeUtils.unescapeHtml4(updateCategoryies);
            updateCategoryies = StringEscapeUtils.unescapeXml(updateCategoryies);
            updateCategoryies = updateCategoryies.replaceAll("\\[|\\]", "");

            String[] categoryArray = updateCategoryies.split(", ");
            List<String> categoryList = Arrays.stream(categoryArray)
                    .map(String::trim)
                    .map(category -> category.replaceAll("&amp;#34;", "\""))
                    .collect(Collectors.toList());

            List<Category> bookCategories = Arrays.stream(updateCategoryies.split(","))
                    .map(String::trim)
                    .map(categoryName -> StringEscapeUtils.unescapeHtml4(categoryName))
                    .map(categoryName -> StringEscapeUtils.unescapeXml(categoryName))
                    .map(categoryName -> categoryName.replaceAll("\"", ""))
                    .map(categoryName -> findOrCreateCategory(categoryName, finalBook))
                    .collect(Collectors.toList());

            String updateAuthors = addBookRequestDTO.getAuthors();

            updateAuthors = StringEscapeUtils.unescapeHtml4(updateAuthors);
            updateAuthors = StringEscapeUtils.unescapeXml(updateAuthors);
            updateAuthors = updateAuthors.replaceAll("\\[|\\]", "");
            List<String> authorList = Arrays.stream(categoryArray)
                    .map(String::trim)
                    .map(category -> category.replaceAll("&amp;#34;", "\""))
                    .collect(Collectors.toList());
            List<Author> bookAuthors = Arrays.stream(updateAuthors.split(","))
                    .map(String::trim)
                    .map(authorName -> StringEscapeUtils.unescapeHtml4(authorName))
                    .map(authorName -> StringEscapeUtils.unescapeXml(authorName))
                    .map(authorName -> authorName.replaceAll("\"", ""))
                    .map(authorName -> findOrCreateAuthor(authorName, finalBook))
                    .collect(Collectors.toList());
            book.setCategories(bookCategories.stream().map(Category::getId).collect(Collectors.toList()));
            book.setAuthors(bookAuthors.stream().map(Author::getId).collect(Collectors.toList()));
            Book newBook = bookRepository.save(book);
            bookDetailRepository.save(bookDetail);

            BookDetailDTO bookDetailDTO = convertToBookDetailDTO(newBook);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Thêm sách thành công")
                    .data(bookDetailDTO)
                    .build());

        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                        .success(false)
                        .message("Lỗi khi thêm sách")
                        .data(e.getMessage())
                        .build());
        }
    }
    public ResponseEntity<GenericResponse> updateBook(String userId, String bookId, UpdateBookRequestDTO updateBookRequestDTO, MultipartHttpServletRequest imageRequest){
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()) || "Manager".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền cập nhật sách")
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
            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));
            if (!optionalBook.isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách")
                        .data(null)
                        .build());
            }
            Book book = optionalBook.get();
            Optional<BookDetail> optionalBookDetail = bookDetailRepository.findByBook(book.getId());
            if (!optionalBookDetail.isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy chi tiết sách")
                        .data(null)
                        .build());
            }
            BookDetail bookDetail = optionalBookDetail.get();

            bookDetail.setPageNumbers(Integer.parseInt(updateBookRequestDTO.getPageNumbers()));
            book.setStock(Integer.parseInt(updateBookRequestDTO.getStock()));
            book.setPrice(Double.parseDouble(updateBookRequestDTO.getPrice()));
            MultipartFile image = imageRequest.getFile("image");
            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap()).get("secure_url").toString();
                    bookDetail.setImage(imageUrl);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                            .success(false)
                            .message("Lỗi upload ảnh")
                            .data(null)
                            .build());
                }
            }
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.map(updateBookRequestDTO, book);
            final Book finalBook = book;

            String updateCategoryies = updateBookRequestDTO.getCategories();
            updateCategoryies = StringEscapeUtils.unescapeHtml4(updateCategoryies);
            updateCategoryies = StringEscapeUtils.unescapeXml(updateCategoryies);
            updateCategoryies = updateCategoryies.replaceAll("\\[|\\]", "");

            String[] categoryArray = updateCategoryies.split(", ");
            List<String> categoryList = Arrays.stream(categoryArray)
                    .map(String::trim)
                    .map(category -> category.replaceAll("&amp;#34;", "\""))
                    .collect(Collectors.toList());

            List<Category> bookCategories = Arrays.stream(updateCategoryies.split(","))
                    .map(String::trim)
                    .map(categoryName -> StringEscapeUtils.unescapeHtml4(categoryName))
                    .map(categoryName -> StringEscapeUtils.unescapeXml(categoryName))
                    .map(categoryName -> categoryName.replaceAll("\"", ""))
                    .map(categoryName -> findOrCreateCategory(categoryName, finalBook))
                    .collect(Collectors.toList());
            List<ObjectId> categoryIds = book.getCategories();

            for (ObjectId categoryId : categoryIds) {
                Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
                if (optionalCategory.isPresent()) {
                    Category category = optionalCategory.get();
                    List<ObjectId> cateBooks = category.getBooks();
                    cateBooks.remove(book.getId());
                    category.setBooks(cateBooks);
                    categoryRepository.save(category);
                }
            }
            String updateAuthors = updateBookRequestDTO.getAuthors();

            updateAuthors = StringEscapeUtils.unescapeHtml4(updateAuthors);
            updateAuthors = StringEscapeUtils.unescapeXml(updateAuthors);
            updateAuthors = updateAuthors.replaceAll("\\[|\\]", "");
            List<String> authorList = Arrays.stream(categoryArray)
                    .map(String::trim)
                    .map(category -> category.replaceAll("&amp;#34;", "\""))
                    .collect(Collectors.toList());
            List<Author> bookAuthors = Arrays.stream(updateAuthors.split(","))
                    .map(String::trim)
                    .map(authorName -> StringEscapeUtils.unescapeHtml4(authorName))
                    .map(authorName -> StringEscapeUtils.unescapeXml(authorName))
                    .map(authorName -> authorName.replaceAll("\"", ""))
                    .map(authorName -> findOrCreateAuthor(authorName, finalBook))
                    .collect(Collectors.toList());
            book.setCategories(bookCategories.stream().map(Category::getId).collect(Collectors.toList()));
            book.setAuthors(bookAuthors.stream().map(Author::getId).collect(Collectors.toList()));
            book = bookRepository.save(book);
            bookDetailRepository.save(bookDetail);
            BookDetailDTO bookDetailDTO = convertToBookDetailDTO(book);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật sách thành công")
                    .data(bookDetailDTO)
                    .build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật sách")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteBook(String userId, String bookId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền cập nhật sách")
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

            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));
            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();
                book.setDeleted(true);
                bookRepository.save(book);

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Xóa sách thành công")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách ")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa sách")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> reviewBook(String userId, String bookId, ReviewBookRequestDTO reviewBookRequestDTO) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy người dùng")
                        .data(null)
                        .build());
            }
            User user = optionalUser.get();
            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));
            if (optionalBook.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách")
                        .data(null)
                        .build());
            }
            Book book = optionalBook.get();
            Review review = new Review();
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.map(reviewBookRequestDTO, review);
            review.setUser(new ObjectId(userId));
            review = reviewRepository.save(review);
            List<ObjectId> reviews = book.getReviews();
            reviews.add(review.getId());
            book.setReviews(reviews);
            book = bookRepository.save(book);
            BookDetailDTO bookDetailDTO = convertToBookDetailDTO(book);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Đánh giá sách thành công")
                    .data(bookDetailDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi đánh giá sách")
                    .data(e.getMessage())
                    .build());
        }
    }
    private List<Book> findRelatedBooks(List<ObjectId> authorIds, List<ObjectId> categoryIds) {
        return bookRepository.findRelatedBooksByAuthorsAndCategories(authorIds, categoryIds);
    }
    @Cacheable("bookDTOCache")
    public BookDTO convertToBookDTO(Book book) {

        ModelMapper modelMapper = new ModelMapper();
        BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
        bookDTO.set_id(book.getId().toString());

        Optional<BookDetail> bookDetailOptional = bookDetailRepository.findByBook(book.getId());
        bookDetailOptional.ifPresent(bookDetail -> {
            bookDTO.setDescription(bookDetail.getDescription());
            bookDTO.setIsbn(bookDetail.getIsbn());
            bookDTO.setImage(bookDetail.getImage());
            bookDTO.setPublisher(bookDetail.getPublisher());
        });

        List<ObjectId> categoryIds = book.getCategories();
        List<ObjectId> authorIds = book.getAuthors();

        List<Category> categories = categoryRepository.findAllByIdIn(categoryIds);
        List<Author> authors = authorRepository.findAllByIdIn(authorIds);

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
        bookDTO.setCategories(categoryDTOs);

        List<AuthorDTO> authorDTOs = authors.stream()
                .map(author -> modelMapper.map(author, AuthorDTO.class))
                .collect(Collectors.toList());
        bookDTO.setAuthors(authorDTOs);

        return bookDTO;
    }
    @Cacheable("bookDetailDTOCache")
    public BookDetailDTO convertToBookDetailDTO(Book book) {
        ModelMapper modelMapper = new ModelMapper();
        BookDetailDTO bookDetailDTO = modelMapper.map(book, BookDetailDTO.class);
        bookDetailDTO.set_id(book.getId().toString());
        bookDetailRepository.findByBook(book.getId())
                .ifPresent(bookDetail -> modelMapper.map(bookDetail, bookDetailDTO));

        List<Category> categories = categoryRepository.findAllByIdIn(book.getCategories());
        List<Author> authors = authorRepository.findAllByIdIn(book.getAuthors());
        List<Review> reviews = reviewService.findReviewsByIds(book.getReviews());

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
        bookDetailDTO.setCategories(categoryDTOs);

        List<AuthorDTO> authorDTOs = authors.stream()
                .map(author -> modelMapper.map(author, AuthorDTO.class))
                .collect(Collectors.toList());
        bookDetailDTO.setAuthors(authorDTOs);

        List<ReviewDTO> reviewDTOS = reviews.stream()
                .map(review -> convertReviewToDTO(review))
                .collect(Collectors.toList());
        bookDetailDTO.setReviews(reviewDTOS);

        return bookDetailDTO;
    }

    private ReviewDTO convertReviewToDTO(Review review){
        ModelMapper modelMapper = new ModelMapper();
        ReviewDTO reviewDTO = modelMapper.map(review, ReviewDTO.class);
        Optional<User> optionalUser = userRepository.findById(review.getUser().toString());
        if(optionalUser.isPresent()){
            UserPostDTO userPostDTO = modelMapper.map(optionalUser.get(), UserPostDTO.class);
            reviewDTO.setUser(userPostDTO);
        }
        return reviewDTO;
    }

    public Category findOrCreateCategory(String categoryName, Book book) {
        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(categoryName);
        Category category = optionalCategory.orElseGet(() -> categoryRepository.save(new Category(categoryName)));
        List<ObjectId> books = category.getBooks();
        books.add((book.getId()));
        category.setBooks(books);
        categoryRepository.save(category);
        return category;
    }

    public Author findOrCreateAuthor(String authorName, Book book) {
        Optional<Author> optionalAuthor = authorRepository.findByAuthorName(authorName);
        Author author = optionalAuthor.orElseGet(() -> authorRepository.save(new Author(authorName)));
        List<ObjectId> books = author.getBooks();
        books.add((book.getId()));
        author.setBooks(books);
        authorRepository.save(author);
        return author;
    }

}

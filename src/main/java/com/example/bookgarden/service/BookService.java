package com.example.bookgarden.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.*;
import com.example.bookgarden.exception.AccessDeniedException;
import com.example.bookgarden.exception.NotFoundException;
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
import org.springframework.transaction.annotation.Transactional;
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
    private CategoryRepository categoryRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private  ReviewService reviewService;
    private void checkAdminPermission(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

        if (!"Admin".equals(user.getRole())) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện thao tác này");
        }
    }
    private void checkAdminAndManagerPermission(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

        if (!("Admin".equals(user.getRole())||"Manager".equals((user.getRole())))) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện thao tác này");
        }
    }
    public ResponseEntity<GenericResponse> getAllBooks() {
        try {
            List<Book> books = bookRepository.findByIsDeletedFalse();
            List<BookDTO> bookDTOs = books.stream()
                    .map(this::convertToBookDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách sách thành công!")
                    .data(bookDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách sách")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> getAllDeletedBooks(String userId) {
        try {
            checkAdminPermission(userId);
            List<Book> books = bookRepository.findByIsDeletedTrue();

            List<BookDTO> bookDTOs = books.stream()
                    .map(this::convertToBookDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách sách bị xóa thành công!")
                    .data(bookDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi lấy danh sách sách bị xóa!")
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
                        .message("Lấy chi tiết sách thành công!")
                        .data(bookDetailDTO)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách!")
                        .data(null)
                        .build());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message("Không tìm thấy sách!")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy chi tiết sách!")
                    .data(e.getMessage())
                    .build());
        }
    }
    @Cacheable("relatedBooksCache")
    public ResponseEntity<GenericResponse> getRelatedBooks(String bookId) {
        try {
            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));

            if (optionalBook.isPresent()) {
                Book bookEntity = optionalBook.get();

                List<Book> relatedBooks = findRelatedBooks(bookEntity.getAuthors(), bookEntity.getCategories());
                relatedBooks.removeIf(b -> b.getId().equals(bookEntity.getId()));
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
                        .message("Không tìm thấy sách")
                        .build());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message("Không tìm thấy sách")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách sách liên quan")
                    .data(e.getMessage())
                    .build());
        }
    }
    @Cacheable("bestSellerBooksCache")
    public ResponseEntity<GenericResponse> getBestSellerBooks() {
        try {
            List<Book> books = bookRepository.findTop10BySoldQuantityIsNotNullOrderBySoldQuantityDesc();

            List<BookDTO> bookDTOs = books.stream()
                    .map(this::convertToBookDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách sách bán chạy thành công!")
                    .data(bookDTOs)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách sách bán chạy!")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> addBook(String userId, AddBookRequestDTO addBookRequestDTO, MultipartHttpServletRequest imageRequest){
        try {
            checkAdminAndManagerPermission(userId);

            Optional<BookDetail> existingBookDetail = bookDetailRepository.findByIsbn(addBookRequestDTO.getIsbn());
            if (existingBookDetail.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Sách với ISBN đã tồn tại")
                        .data(null)
                        .build());
            }

            Book book = new Book();
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.map(addBookRequestDTO, book);

            BookDetail bookDetail = new BookDetail();
            modelMapper.map(addBookRequestDTO, bookDetail);
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

            List<Category> bookCategories = getCategoriesFromString(addBookRequestDTO.getCategories(), book);
            List<Author> bookAuthors = getAuthorsFromString(addBookRequestDTO.getAuthors(), book);

            book.setCategories(bookCategories.stream().map(Category::getId).collect(Collectors.toList()));
            book.setAuthors(bookAuthors.stream().map(Author::getId).collect(Collectors.toList()));

            Book newBook = bookRepository.save(book);
            bookDetail.setBook(book.getId());
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
    private List<Category> getCategoriesFromString(String categoriesStr, Book book) {
        String[] categoryArray = categoriesStr.replaceAll("[\\[\\]]", "").split(",");
        return Arrays.stream(categoryArray)
                .map(String::trim)
                .map(categoryName -> StringEscapeUtils.unescapeHtml4(categoryName))
                .map(categoryName -> StringEscapeUtils.unescapeXml(categoryName))
                .map(categoryName -> categoryName.replaceAll("\"", ""))
                .map(categoryName -> findOrCreateCategory(categoryName, book))
                .collect(Collectors.toList());
    }
    private List<Author> getAuthorsFromString(String authorsStr, Book book) {
        String[] authorArray = authorsStr.replaceAll("[\\[\\]]", "").split(",");
        return Arrays.stream(authorArray)
                .map(String::trim)
                .map(authorName -> StringEscapeUtils.unescapeHtml4(authorName))
                .map(authorName -> StringEscapeUtils.unescapeXml(authorName))
                .map(authorName -> authorName.replaceAll("\"", ""))
                .map(authorName -> findOrCreateAuthor(authorName, book))
                .collect(Collectors.toList());
    }
    public ResponseEntity<GenericResponse> updateBook(String userId, String bookId, UpdateBookRequestDTO updateBookRequestDTO, MultipartHttpServletRequest imageRequest) {
        try {
            checkAdminAndManagerPermission(userId);

            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));
            if (!optionalBook.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách")
                        .data(null)
                        .build());
            }

            Book book = optionalBook.get();
            Optional<BookDetail> optionalBookDetail = bookDetailRepository.findByBook(book.getId());
            if (!optionalBookDetail.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy chi tiết sách")
                        .data(null)
                        .build());
            }

            BookDetail bookDetail = optionalBookDetail.get();
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.map(updateBookRequestDTO, book);
            modelMapper.map(updateBookRequestDTO, bookDetail);

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

            List<Category> bookCategories = getCategoriesFromString(updateBookRequestDTO.getCategories(), book);
            List<Author> bookAuthors = getAuthorsFromString(updateBookRequestDTO.getAuthors(), book);

            // Xóa sách khỏi các danh mục hiện tại
            for (ObjectId categoryId : book.getCategories()) {
                categoryRepository.findById(categoryId).ifPresent(category -> {
                    category.getBooks().remove(book.getId());
                    categoryRepository.save(category);
                });
            }

            book.setCategories(bookCategories.stream().map(Category::getId).collect(Collectors.toList()));
            book.setAuthors(bookAuthors.stream().map(Author::getId).collect(Collectors.toList()));

            bookRepository.save(book);
            bookDetailRepository.save(bookDetail);

            BookDetailDTO bookDetailDTO = convertToBookDetailDTO(book);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật sách thành công")
                    .data(bookDetailDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật sách")
                    .data(e.getMessage())
                    .build());
        }
    }
    public ResponseEntity<GenericResponse> deleteBook(String userId, String bookId) {
        try {
            checkAdminPermission(userId);

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
    }public ResponseEntity<GenericResponse> restoreBook(String userId, String bookId) {
        try {
            checkAdminPermission(userId);

            Optional<Book> optionalBook = bookRepository.findById(new ObjectId(bookId));
            if (optionalBook.isPresent()) {
                Book book = optionalBook.get();
                if (!book.isDeleted()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                            .success(false)
                            .message("Sách chưa bị xóa")
                            .data(null)
                            .build());
                }
                book.setDeleted(false);
                bookRepository.save(book);

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Khôi phục sách thành công")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy sách")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi khôi phục sách")
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
        Set<Book> relatedBooks = new LinkedHashSet<>(bookRepository.findRelatedBooksByAuthorsAndCategories(authorIds, categoryIds));
        relatedBooks.addAll(bookRepository.findRelatedBooksByAuthors(authorIds));
        relatedBooks.addAll(bookRepository.findRelatedBooksByCategories(categoryIds));
        return new ArrayList<>(relatedBooks);
    }

    @Cacheable("bookDTOCache")
    public BookDTO convertToBookDTO(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

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
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllByIdIn(categoryIds);
            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(category -> modelMapper.map(category, CategoryDTO.class))
                    .collect(Collectors.toList());
            bookDTO.setCategories(categoryDTOs);
        } else {
            bookDTO.setCategories(Collections.emptyList());
        }

        List<ObjectId> authorIds = book.getAuthors();
        if (authorIds != null && !authorIds.isEmpty()) {
            List<Author> authors = authorRepository.findAllByIdIn(authorIds);
            List<AuthorDTO> authorDTOs = authors.stream()
                    .map(author -> modelMapper.map(author, AuthorDTO.class))
                    .collect(Collectors.toList());
            bookDTO.setAuthors(authorDTOs);
        } else {
            bookDTO.setAuthors(Collections.emptyList());
        }
        return bookDTO;
    }

    @Cacheable("bookDetailDTOCache")
    public BookDetailDTO convertToBookDetailDTO(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        ModelMapper modelMapper = new ModelMapper();
        BookDetailDTO bookDetailDTO = modelMapper.map(book, BookDetailDTO.class);
        bookDetailDTO.set_id(book.getId().toString());

        Optional<BookDetail> bookDetailOptional = bookDetailRepository.findByBook(book.getId());
        bookDetailOptional.ifPresent(bookDetail -> {
            bookDetailDTO.setDescription(bookDetail.getDescription());
            bookDetailDTO.setIsbn(bookDetail.getIsbn());
            bookDetailDTO.setImage(bookDetail.getImage());
            bookDetailDTO.setPublisher(bookDetail.getPublisher());
        });

        List<ObjectId> categoryIds = book.getCategories();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllByIdIn(categoryIds);
            List<CategoryDTO> categoryDTOs = categories.stream()
                    .map(category -> modelMapper.map(category, CategoryDTO.class))
                    .collect(Collectors.toList());
            bookDetailDTO.setCategories(categoryDTOs);
        } else {
            bookDetailDTO.setCategories(Collections.emptyList());
        }

        List<ObjectId> authorIds = book.getAuthors();
        if (authorIds != null && !authorIds.isEmpty()) {
            List<Author> authors = authorRepository.findAllByIdIn(authorIds);
            List<AuthorDTO> authorDTOs = authors.stream()
                    .map(author -> modelMapper.map(author, AuthorDTO.class))
                    .collect(Collectors.toList());
            bookDetailDTO.setAuthors(authorDTOs);
        } else {
            bookDetailDTO.setAuthors(Collections.emptyList());
        }

        List<ObjectId> reviewIds = book.getReviews();
        if (reviewIds != null && !reviewIds.isEmpty()) {
            List<Review> reviews = reviewRepository.findAllByIdIn(reviewIds);
            List<ReviewDTO> reviewDTOs = reviews.stream()
                    .map(this::convertReviewToDTO)
                    .collect(Collectors.toList());
            bookDetailDTO.setReviews(reviewDTOs);
        } else {
            bookDetailDTO.setReviews(Collections.emptyList());
        }

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

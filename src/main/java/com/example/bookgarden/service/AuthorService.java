package com.example.bookgarden.service;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Author;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.exception.ForbiddenException;
import com.example.bookgarden.repository.AuthorRepository;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<GenericResponse> getAllAuthors() {
        try {
            List<Author> authors = authorRepository.findAll();

            List<AuthorResponseDTO> authorDTOs = authors.stream()
                    .map(this::convertToAuthorDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách tác giả thành công")
                    .data(authorDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi lấy danh sách tác giả: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getAuthorById(String authorId){
        try {
            Optional<Author> optionalAuthor = authorRepository.findById(authorId);
            if (optionalAuthor.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy tác giả ")
                        .data(null)
                        .build());
            }
            Author author = optionalAuthor.get();
            AuthorResponseDTO authorResponseDTO = convertToAuthorDTO(author);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy thông tin tác giả thành công")
                    .data(authorResponseDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin tác giả")
                    .data(e.getMessage())
                    .build());
        }
    }
    @CacheEvict(value = "authorDTOCache", allEntries = true)
    public ResponseEntity<GenericResponse> updateAuthor(String userId, String authorId, UpdateAuthorRequestDTO updateAuthorRequestDTO) {
        try {
            checkAdminAndManagerPermission(userId);
            Optional<Author> optionalAuthor = authorRepository.findById(authorId);
            if (optionalAuthor.isPresent()) {
                Author author = optionalAuthor.get();
                author.setAuthorName(updateAuthorRequestDTO.getAuthorName());

                Author updatedAuthor = authorRepository.save(author);
                AuthorResponseDTO authorResponseDTO = convertToAuthorDTO(updatedAuthor);

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật tác giả thành công")
                        .data(authorResponseDTO)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy tác giả")
                        .data(null)
                        .build());
            }
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật tác giả: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }
    @CacheEvict(value = "authorDTOCache", allEntries = true)
    public ResponseEntity<GenericResponse> addAuthor(String userId, UpdateAuthorRequestDTO addAuthorRequestDTO){
        try {
            checkAdminAndManagerPermission(userId);
            Optional<Author> existingAuthor = authorRepository.findByAuthorNameAndIsDeletedFalse(addAuthorRequestDTO.getAuthorName());
            if (existingAuthor.isPresent()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Tác giả " + existingAuthor.get().getAuthorName() + " đã tồn tại")
                        .data(null)
                        .build());
            }
            Author author = new Author();
            author.setAuthorName(addAuthorRequestDTO.getAuthorName());
            author.setBooks(new ArrayList<>());

            authorRepository.save(author);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Thêm tác giả thành công")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thêm danh mục")
                    .data(e.getMessage())
                    .build());
        }
    }
    @CacheEvict(value = "authorDTOCache", allEntries = true)
    public ResponseEntity<GenericResponse> deleteAuthor(String userId, String authorId) {
        try {
            checkAdminPermission(userId);
            Optional<Author> authorOptional = authorRepository.findByIdAndIsDeletedFalse(new ObjectId(authorId));
            if (authorOptional.isPresent()) {
                Author author = authorOptional.get();
                author.setIsDeleted(true);

                List<Book> books = bookRepository.findByAuthorsContains(new ObjectId(authorId));

                for (Book book : books) {
                    book.setDeleted(true);
                    bookRepository.save(book);
                }

                authorRepository.save(author);

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Xóa tác giả thành công")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy tác giả")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa tác giả: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }
    @Cacheable("authorDTOCache")
    public AuthorResponseDTO convertToAuthorDTO(Author author) {
        AuthorResponseDTO authorResponseDTO = new AuthorResponseDTO();
        authorResponseDTO.setId(author.getId().toString());
        authorResponseDTO.setAuthorName(author.getAuthorName());

        List<Book> books = bookRepository.findAllById(author.getBooks());
        List<BookDTO> bookDTOs = books.stream()
                .map(bookService::convertToBookDTO)
                .collect(Collectors.toList());
        authorResponseDTO.setBooks(bookDTOs);

        return authorResponseDTO;
    }
    private void checkAdminPermission(String userId) throws ForbiddenException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            if (!"Admin".equals(optionalUser.get().getRole())) {
                throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này");
            }
        } else {
            throw new ForbiddenException("Người dùng không tồn tại");
        }
    }
    private void checkAdminAndManagerPermission(String userId) throws ForbiddenException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            if (!"Admin".equals(optionalUser.get().getRole()) && !"Manager".equals(optionalUser.get().getRole())) {
                throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này");
            }
        } else {
            throw new ForbiddenException("Người dùng không tồn tại");
        }
    }
}

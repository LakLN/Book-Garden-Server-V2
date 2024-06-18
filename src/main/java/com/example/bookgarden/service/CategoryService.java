package com.example.bookgarden.service;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.Category;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.CategoryRepository;
import com.example.bookgarden.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

import javax.persistence.Cacheable;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    @Autowired
    private CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookService bookService;

    Cache cache;

    public ResponseEntity<GenericResponse> addCategory(String userId, AddCategoryRequestDTO addCategoryRequestDTO){
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền thêm danh mục sách")
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
            Optional<Category> existingCategory = categoryRepository.findByCategoryName(addCategoryRequestDTO.getCategoryName());
            if (existingCategory.isPresent()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Danh mục " + existingCategory.get().getCategoryName() + " đã tồn tại")
                        .data(null)
                        .build());
            }
            Category category = new Category();
            category.setCategoryName(addCategoryRequestDTO.getCategoryName());
            category.setBooks(new ArrayList<>());

            categoryRepository.save(category);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Thêm danh mục thành công")
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

    public ResponseEntity<GenericResponse> getAllCategories(){
        try {
            List<Category> categories = categoryRepository.findAll();
            List<CategoryResponseDTO> categoryDTOs = categories.stream()
                    .map(this::convertToCategoryDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách danh mục thành công")
                    .data(categoryDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách danh mục")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getCategoryById(String categoryId) {
        try {
            Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
            if (categoryOptional.isPresent()) {
                CategoryResponseDTO categoryDTO = convertToCategoryDTO(categoryOptional.get());
                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Lấy thông tin danh mục thành công")
                        .data(categoryDTO)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy danh mục")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin danh mục")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> updateCategories(String userId, String categoryId, AddCategoryRequestDTO updateCategoryRequestDTO) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền cập nhật danh mục sách")
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
            Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();

                category.setCategoryName(updateCategoryRequestDTO.getCategoryName());
                Category updatedCategory = categoryRepository.save(category);

                CategoryResponseDTO updatedCategoryDTO = convertToCategoryDTO(updatedCategory);

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Cập nhật danh mục thành công")
                        .data(updatedCategoryDTO)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy danh mục")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật danh mục")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> deleteCategory(String userId, String categoryId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                if (!("Admin".equals(optionalUser.get().getRole()))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                            .success(false)
                            .message("Bạn không có quyền xóa danh mục sách")
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
            Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();

                List<Book> books = bookRepository.findAllById(category.getBooks());

                books.forEach(book -> {
                    book.getCategories().remove(category.getId());
                    bookRepository.save(book);

                    if (book.getCategories().isEmpty()) {
                        Category uncategorizedCategory = categoryRepository.findByCategoryName("Chưa phân loại").orElse(null);
                        if (uncategorizedCategory != null) {
                            book.getCategories().add(uncategorizedCategory.getId());
                            bookRepository.save(book);
                        }
                    }

                });

                categoryRepository.deleteById(categoryId);

                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Xóa danh mục thành công")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy danh mục")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa danh mục")
                    .data(e.getMessage())
                    .build());
        }
    }
    private CategoryResponseDTO convertToCategoryDTO(Category category) {
        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setId(category.getId().toString());
        categoryResponseDTO.setCategoryName(category.getCategoryName());

        List<Book> books = bookRepository.findAllById(category.getBooks());

        List<BookDTO> bookDTOs = books.stream()
                .map(book -> bookService.convertToBookDTO(book))
                .collect(Collectors.toList());
        categoryResponseDTO.setBooks(bookDTOs);

        return categoryResponseDTO;
    }

    public List<Category> findCategoriesByIds(List<ObjectId> categoryIds) {
        List<Category> categories = cache.get("categories:" + categoryIds, () -> {
            return categoryRepository.findAllByIdIn(categoryIds);
        });
        return categories;
    }
}

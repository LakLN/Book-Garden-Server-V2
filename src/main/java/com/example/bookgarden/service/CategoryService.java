package com.example.bookgarden.service;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.Category;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.CategoryRepository;
import com.example.bookgarden.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;
    @CacheEvict(value = "categoryCache", allEntries = true)
    public ResponseEntity<GenericResponse> addCategory(String userId, AddCategoryRequestDTO addCategoryRequestDTO){
        try {
            if (!checkAdminPermission(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Bạn không có quyền thêm danh mục sách")
                        .data(null)
                        .build());
            }

            Optional<Category> existingCategory = categoryRepository.findByCategoryName(addCategoryRequestDTO.getCategoryName());
            if (existingCategory.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
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

    @Cacheable(value = "categoryCache")
    public ResponseEntity<GenericResponse> getAllCategories() {
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

    @Cacheable(value = "categoryCache", key = "#categoryId")
    public ResponseEntity<GenericResponse> getCategoryById(String categoryId) {
        try {
            Optional<Category> categoryOptional = categoryRepository.findById(new ObjectId(categoryId));
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

    @CachePut(value = "categoryCache", key = "#categoryId")
    public ResponseEntity<GenericResponse> updateCategories(String userId, String categoryId, AddCategoryRequestDTO updateCategoryRequestDTO) {
        try {
            if (!checkAdminPermission(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Bạn không có quyền cập nhật danh mục sách")
                        .data(null)
                        .build());
            }
            Optional<Category> categoryOptional = categoryRepository.findById(new ObjectId(categoryId));
            if (!categoryOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy danh mục")
                        .data(null)
                        .build());
            }

            Category category = categoryOptional.get();
            category.setCategoryName(updateCategoryRequestDTO.getCategoryName());
            Category updatedCategory = categoryRepository.save(category);
            CategoryResponseDTO updatedCategoryDTO = convertToCategoryDTO(updatedCategory);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật danh mục thành công")
                    .data(updatedCategoryDTO)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật danh mục: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @CacheEvict(value = "categoryCache", key = "#categoryId")
    public ResponseEntity<GenericResponse> deleteCategory(String userId, String categoryId) {
        try {
            if (!checkAdminPermission(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Bạn không có quyền xóa danh mục sách")
                        .data(null)
                        .build());
            }

            Optional<Category> categoryOptional = categoryRepository.findById(new ObjectId(categoryId));
            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();

                List<Book> books = bookRepository.findByCategoriesContains(new ObjectId(categoryId));

                for (Book book : books) {
                    book.getCategories().remove(category.getId());
                    if (book.getCategories().isEmpty()) {
                        Category uncategorizedCategory = categoryRepository.findByCategoryName("Chưa phân loại")
                                .orElseGet(() -> {
                                    Category newCategory = new Category();
                                    newCategory.setCategoryName("Chưa phân loại");
                                    return categoryRepository.save(newCategory);
                                });
                        book.getCategories().add(uncategorizedCategory.getId());
                    }
                    bookRepository.save(book);
                }

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
                    .message("Lỗi khi xóa danh mục: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    private CategoryResponseDTO convertToCategoryDTO(Category category) {
        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setId(category.getId().toString());
        categoryResponseDTO.setCategoryName(category.getCategoryName());

        List<Book> books = bookRepository.findAllById(category.getBooks());

        List<BookDTO> bookDTOs = books.stream()
                .map(bookService::convertToBookDTO)
                .collect(Collectors.toList());
        categoryResponseDTO.setBooks(bookDTOs);

        return categoryResponseDTO;
    }

    public boolean checkAdminPermission(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.isPresent() && "Admin".equals(optionalUser.get().getRole());
    }
}

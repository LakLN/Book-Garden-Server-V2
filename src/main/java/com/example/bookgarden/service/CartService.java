package com.example.bookgarden.service;

import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.Book;
import com.example.bookgarden.entity.Cart;
import com.example.bookgarden.entity.CartItem;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.exception.NotFoundException;
import com.example.bookgarden.repository.BookRepository;
import com.example.bookgarden.repository.CartItemRepository;
import com.example.bookgarden.repository.CartRepository;
import com.example.bookgarden.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookService bookService;

    public ResponseEntity<GenericResponse> addToCart(String userId, AddToCartRequestDTO addToCartRequestDTO) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Người dùng không tồn tại"));

            Book book = bookRepository.findById(new ObjectId(addToCartRequestDTO.getBookID()))
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sách"));

            Cart cart = getOrCreateCart(user);

            Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndBook(cart.getId(), book.getId());

            if (existingCartItem.isPresent()) {
                CartItem cartItem = existingCartItem.get();
                int newQuantity = cartItem.getQuantity() + addToCartRequestDTO.getQuantity();

                if (newQuantity <= book.getStock()) {
                    cartItem.setQuantity(newQuantity);
                    cartItemRepository.save(cartItem);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                            .success(false)
                            .message("Số lượng sách trong giỏ hàng vượt quá tồn kho")
                            .data(null)
                            .build());
                }
            } else {
                int requestedQuantity = addToCartRequestDTO.getQuantity();
                if (requestedQuantity <= book.getStock()) {
                    CartItem cartItem = new CartItem();
                    cartItem.setCart(cart.getId());
                    cartItem.setBook(book.getId());
                    cartItem.setQuantity(requestedQuantity);
                    cartItem = cartItemRepository.save(cartItem);

                    cart.getItems().add(cartItem.getId());
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                            .success(false)
                            .message("Số lượng sách trong giỏ hàng vượt quá tồn kho")
                            .data(null)
                            .build());
                }
            }

            Cart updatedCart = cartRepository.save(cart);
            CartDTO cartDTO = convertToCartDTO(updatedCart);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Đã thêm sách vào giỏ hàng thành công")
                    .data(cartDTO)
                    .build());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi thêm sách vào giỏ hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    private Cart getOrCreateCart(User user) {
        if (user.getCart() == null) {
            Cart cart = new Cart(new ObjectId(user.getId()));
            cart = cartRepository.save(cart);
            user.setCart(cart.getId().toString());
            userRepository.save(user);
            return cart;
        } else {
            return cartRepository.findByUser(new ObjectId(user.getId()))
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy giỏ hàng của người dùng"));
        }
    }

    public ResponseEntity<GenericResponse> getCart(String userId) {
        try {
            Optional<Cart> optionalCart = cartRepository.findByUser(new ObjectId(userId));
            if (optionalCart.isPresent()) {
                Cart cart = optionalCart.get();
                CartDTO cartDTO = convertToCartDTO(cart);
                return ResponseEntity.ok(GenericResponse.builder()
                        .success(true)
                        .message("Lấy thông tin giỏ hàng thành công")
                        .data(cartDTO)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy giỏ hàng của user")
                        .data(null)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy thông tin giỏ hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> updateCartItem(String userId, AddToCartRequestDTO updateCartItemRequestDTO) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }

            Optional<Cart> optionalCart = cartRepository.findByUser(new ObjectId(userId));
            if (optionalCart.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy giỏ hàng của người dùng")
                        .data(null)
                        .build());
            }

            Cart cart = optionalCart.get();
            Optional<CartItem> optionalCartItem = cartItemRepository.findByBook(new ObjectId(updateCartItemRequestDTO.getBookID()));
            if (optionalCartItem.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy cart item sản phẩm của người dùng")
                        .data(null)
                        .build());
            }
            CartItem cartItem = optionalCartItem.get();
            if (!cartItem.getCart().equals(cart.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không có quyền cập nhật cartItem này")
                        .data(null)
                        .build());
            }

            int newQuantity = updateCartItemRequestDTO.getQuantity();
            Book book = bookRepository.findById(new ObjectId(updateCartItemRequestDTO.getBookID())).get();
            if (newQuantity <= 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Số lượng phải lớn hơn 0")
                        .data(null)
                        .build());
            } else if (newQuantity > book.getStock()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Số lượng không được vượt quá " + book.getStock() + " sản phẩm tồn kho")
                        .data(null)
                        .build());
            } else {
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            }

            Cart updatedCart = cartRepository.save(cart);
            CartDTO cartDTO = convertToCartDTO(updatedCart);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật cartItem thành công")
                    .data(cartDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật cartItem")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> removeCartItem(String userId, String cartItemId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }

            Optional<Cart> optionalCart = cartRepository.findByUser(new ObjectId(userId));
            if (optionalCart.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy giỏ hàng của người dùng")
                        .data(null)
                        .build());
            }

            Cart cart = optionalCart.get();

            Optional<CartItem> optionalCartItem = cartItemRepository.findById(new ObjectId(cartItemId));
            if (optionalCartItem.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy cartItem trong giỏ hàng")
                        .data(null)
                        .build());
            }

            CartItem cartItem = optionalCartItem.get();

            if (!cartItem.getCart().equals(cart.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không có quyền xóa cartItem này")
                        .data(null)
                        .build());
            }

            cart.getItems().remove(cartItem.getId());
            cartItemRepository.delete(cartItem);

            Cart updatedCart = cartRepository.save(cart);
            CartDTO cartDTO = convertToCartDTO(updatedCart);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Đã xóa cartItem khỏi giỏ hàng thành công")
                    .data(cartDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xóa cartItem khỏi giỏ hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setUser(cart.getUser().toString());

        List<CartItemDTO> cartItemDTOs = new ArrayList<>();
        if (cart.getItems() != null) {
            for (ObjectId cartItemId : cart.getItems()) {
                CartItem cartItem = cartItemRepository.findById(cartItemId).orElse(null);
                if (cartItem != null) {
                    Book book = bookRepository.findById(cartItem.getBook()).orElse(null);
                    if (book != null) {
                        CartItemDTO cartItemDTO = new CartItemDTO();
                        cartItemDTO.set_id(cartItem.getId().toString());
                        cartItemDTO.setBook(bookService.convertToBookDTO(book));
                        cartItemDTO.setQuantity(cartItem.getQuantity());
                        cartItemDTOs.add(cartItemDTO);
                    }
                }
            }
        }
        cartDTO.setItems(cartItemDTOs);
        return cartDTO;
    }

}

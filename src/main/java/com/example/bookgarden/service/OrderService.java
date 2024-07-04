package com.example.bookgarden.service;

import com.example.bookgarden.constant.OrderStatus;
import com.example.bookgarden.dto.*;
import com.example.bookgarden.entity.*;
import com.example.bookgarden.exception.ForbiddenException;
import com.example.bookgarden.repository.*;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    public ResponseEntity<GenericResponse> createOrder(String userId, CreateOrderRequestDTO createOrderRequestDTO){
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }
            User user = optionalUser.get();
            Order order = new Order();
            order.setUser(new ObjectId(userId));
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.map(createOrderRequestDTO, order);
            Optional<Address> optionalAddress = addressRepository.findByAddress(createOrderRequestDTO.getAddress());
            if (optionalAddress.isEmpty()){
                Address newAddress = new Address();
                newAddress.setAddress(createOrderRequestDTO.getAddress());
                addressRepository.save(newAddress);
                List<ObjectId> addresses = user.getAddresses();
                addresses.add(newAddress.getId());
                user.setAddresses(addresses);
                userRepository.save(user);
            }
            List<String> cartItems = createOrderRequestDTO.getCartItems();

            List<ObjectId> cartItemObjectIds = cartItems.stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());

            List<OrderItem> orderItems = cartItemRepository.findAllByIdIn(cartItemObjectIds).stream()
                    .map(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setBook(cartItem.getBook());
                        orderItem.setQuantity(cartItem.getQuantity());

                        Book book = bookRepository.findById(cartItem.getBook()).get();
                        int soldQuantity = book.getSoldQuantity() + cartItem.getQuantity();
                        book.setSoldQuantity(soldQuantity);

                        int stock = book.getStock() - cartItem.getQuantity();
                        book.setStock(stock);

                        bookRepository.save(book);

                        return orderItemRepository.save(orderItem);
                    })
                    .collect(Collectors.toList());
            List<CartItem> cartItemss = cartItemRepository.findAllByIdIn(cartItemObjectIds);
            cartItemRepository.deleteAll(cartItemss);

            List<ObjectId> orderItemIds = orderItems.stream()
                    .map(OrderItem::getId)
                    .collect(Collectors.toList());
            order.setOrderItems(orderItemIds);
            order.setStatus("PENDING");
            Order savedOrder = orderRepository.save(order);

            OrderDTO orderDTO = convertToOrderDTO(savedOrder);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Đã tạo đơn hàng thành công")
                    .data(orderDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi tạo đơn hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getUserOrders(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Người dùng không tồn tại")
                        .data(null)
                        .build());
            }
            List<Order> orders = orderRepository.findByUser(new ObjectId(userId));
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(this::convertToOrderDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách đơn hàng thành công")
                    .data(orderDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách đơn hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getAllOrders(String userId){
        try {
            checkAdminAndManagerPermission(userId);
            List<Order> orders = orderRepository.findAll();
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(this::convertToOrderDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy danh sách đơn hàng thành công")
                    .data(orderDTOs)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách đơn hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> getOrderById(String userId, String orderId) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy người dùng")
                        .data(null)
                        .build());
            }
            Optional<Order> optionalOrder = orderRepository.findById(new ObjectId(orderId));
            if (optionalOrder.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy thông tin đơn hàng")
                        .data(null)
                        .build());
            }
            Order order = optionalOrder.get();
            if (!"Admin".equals(optionalUser.get().getRole()) && !"Manager".equals(optionalUser.get().getRole()) && !order.getUser().equals(new ObjectId(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Bạn không có quyền truy cập đơn hàng này")
                        .data(null)
                        .build());
            }
            OrderDTO orderDTO = convertToOrderDTO(optionalOrder.get());
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Lấy thông tin đơn hàng thành công")
                    .data(orderDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi lấy chi tiết đơn hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> updateOrderStatus(String userId, String orderId, UpdateOrderStatusRequestDTO updateOrderStatusRequestDTO) {
        try {
            checkAdminAndManagerPermission(userId);
            Optional<Order> optionalOrder = orderRepository.findById(new ObjectId(orderId));
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy thông tin đơn hàng")
                        .data(null)
                        .build());
            }
            Order order = optionalOrder.get();
            OrderStatus newStatus = OrderStatus.fromString(updateOrderStatusRequestDTO.getStatus());

            if (!canUpdateOrderStatus(order.getStatus(), newStatus)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("Không thể thay đổi trạng thái đơn hàng")
                        .data(null)
                        .build());
            }
            order.setStatus(updateOrderStatusRequestDTO.getStatus());
            if (updateOrderStatusRequestDTO.getStatus().equals("DELIVERED")){
                order.setPaymentStatus("PAID");
            }
            Order updatedOrder = orderRepository.save(order);
            OrderDTO orderDTO = convertToOrderDTO(updatedOrder);
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Cập nhật trạng thái đơn hàng thành công")
                    .data(orderDTO)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi cập nhật trạng thái đơn hàng")
                    .data(e.getMessage())
                    .build());
        }
    }

    public ResponseEntity<GenericResponse> handlePaymentCallback(PaymentCallBackRequestDTO paymentCallBackRequestDTO){
        try {
            Optional<Order> optionalOrder = orderRepository.findById(new ObjectId(paymentCallBackRequestDTO.getOrderId()));
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                if ("00".equals(paymentCallBackRequestDTO.getResponseCode())) {
                    order.setPaymentStatus("PAID");
                    order.setPaymentDate(new Date());
                    order.setStatus("PROCESSING");
                    order = orderRepository.save(order);
                    OrderDTO orderDTO = convertToOrderDTO(order);

                    return ResponseEntity.ok(GenericResponse.builder()
                            .success(true)
                            .message("Thanh toán thành công")
                            .data(orderDTO)
                            .build());
                } else {
                    return ResponseEntity.ok(GenericResponse.builder()
                            .success(false)
                            .message("Thanh toán thất bại")
                            .build());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Không tìm thấy đơn hàng")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi khi xử lý thanh toán")
                    .data(e.getMessage())
                    .build());
        }
    }
    @Cacheable("orderDTOCache")
    public OrderDTO convertToOrderDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(order, orderDTO);
        orderDTO.set_id(order.getId().toString());
        orderDTO.setStatus(order.getStatus().toString());

        List<OrderItem> orderItems = orderItemRepository.findByIdIn(order.getOrderItems());
        List<ObjectId> bookIds = orderItems.stream()
                .map(OrderItem::getBook)
                .collect(Collectors.toList());

        Map<ObjectId, Book> booksMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(orderItem -> convertToOrderItemDTO(orderItem, booksMap, modelMapper))
                .collect(Collectors.toList());

        orderDTO.setOrderItems(orderItemDTOs);
        return orderDTO;
    }
    @Cacheable("orderItemDTOCache")
    public OrderItemDTO convertToOrderItemDTO(OrderItem orderItem, Map<ObjectId, Book> booksMap, ModelMapper modelMapper) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        modelMapper.map(orderItem, orderItemDTO);
        orderItemDTO.set_id(orderItem.getId().toString());

        Book book = booksMap.get(orderItem.getBook());
        orderItemDTO.setBook(bookService.convertToBookDTO(book));
        return orderItemDTO;
    }
    private boolean canUpdateOrderStatus(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING:
                return newStatus == OrderStatus.DELIVERING || newStatus == OrderStatus.CANCELLED;
            case DELIVERING:
                return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED;
            case DELIVERED:
                return false;
            case CANCELLED:
                return false;
            default:
                return false;
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

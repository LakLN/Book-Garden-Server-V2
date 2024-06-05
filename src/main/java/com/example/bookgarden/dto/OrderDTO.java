package com.example.bookgarden.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String _id;
    private String user;
    private List<OrderItemDTO> orderItems;
    private double totalAmount;
    private String fullName;
    private String address;
    private String phone;
    private String status;
    private Date orderDate;
    private String paymentMethod ;
    private Date paymentDate;
    private double paymentAmount;
    private String paymentStatus ;
}

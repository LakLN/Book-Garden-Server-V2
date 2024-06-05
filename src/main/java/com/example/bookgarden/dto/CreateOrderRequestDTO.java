package com.example.bookgarden.dto;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
public class CreateOrderRequestDTO {
    private List<String> cartItems;
    private double totalAmount;
    private String fullName;
    private String address;
    private String phone;
    private String paymentMethod;
}

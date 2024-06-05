package com.example.bookgarden.dto;

import lombok.Data;

@Data
public class PaymentResponseDTO {
    private String status;
    private String message;
    private String URL;
}

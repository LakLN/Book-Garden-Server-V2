package com.example.bookgarden.dto;

import lombok.Data;

@Data
public class PaymentCallBackRequestDTO {
    private String orderId;
    private String responseCode;
}

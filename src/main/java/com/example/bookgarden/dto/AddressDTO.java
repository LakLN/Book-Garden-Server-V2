package com.example.bookgarden.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String id;
    private String name;
    private String phoneNumber;
    private String address;
    private Boolean isDefault;
}
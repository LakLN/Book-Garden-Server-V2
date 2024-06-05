package com.example.bookgarden.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddressesRequestDTO {
    private List<String> addresses;
}

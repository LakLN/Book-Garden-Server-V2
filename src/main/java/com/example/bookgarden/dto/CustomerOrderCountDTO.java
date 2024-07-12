package com.example.bookgarden.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderCountDTO {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
    private long orderCount;
}

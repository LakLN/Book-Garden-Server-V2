package com.example.bookgarden.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserInfo {
    private String id;
    private String email;
    private String name;
    private String picture;
}

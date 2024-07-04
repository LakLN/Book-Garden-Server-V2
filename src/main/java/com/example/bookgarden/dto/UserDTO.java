package com.example.bookgarden.dto;

import com.example.bookgarden.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
    private String role;
    private String phone;
    private int points;
    private Date birthday;
    private String gender;
    private List<AddressDTO> addresses;
}

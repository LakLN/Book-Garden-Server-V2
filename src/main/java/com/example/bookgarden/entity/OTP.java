package com.example.bookgarden.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otps")
public class OTP {
    @Id
    private String id;
    private String email;
    private String otp;
    private LocalDateTime expirationTime;

    public OTP(String email, String otp, LocalDateTime expirationTime) {
        this.email = email;
        this.otp = otp;
        this.expirationTime = expirationTime;
    }
}

package com.example.bookgarden.service;

import com.example.bookgarden.entity.OTP;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.OTPRepository;
import com.example.bookgarden.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OTPServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private OTPRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OTPService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendRegisterOtp() throws Exception {
        String email = "test@example.com";
        String otp = "123456";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doNothing().when(mailSender).send(any(MimeMessage.class));

        otpService.sendRegisterOtp(email);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(otpRepository, times(1)).save(any(OTP.class));
    }

    @Test
    void testSendForgotPasswordOtp() throws Exception {
        String email = "test@example.com";
        String otp = "123456";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doNothing().when(mailSender).send(any(MimeMessage.class));

        otpService.sendForgotPasswordOtp(email);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(otpRepository, times(1)).save(any(OTP.class));
    }

    @Test
    void testVerifyOtp_Success() {
        String email = "test@example.com";
        String otp = "123456";

        OTP otpEntity = new OTP(email, otp, LocalDateTime.now().plusMinutes(5));
        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(otpEntity));

        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = otpService.verifyOtp(email, otp);

        assertTrue(result);
        assertTrue(user.getIsVerified());
        assertTrue(user.getIsActive());

        verify(otpRepository, times(1)).delete(any(OTP.class));
    }

    @Test
    void testVerifyOtp_Failure() {
        String email = "test@example.com";
        String otp = "123456";

        when(otpRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = otpService.verifyOtp(email, otp);

        assertFalse(result);
    }
}

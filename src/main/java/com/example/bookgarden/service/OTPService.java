package com.example.bookgarden.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.example.bookgarden.entity.OTP;
import com.example.bookgarden.entity.User;
import com.example.bookgarden.repository.OTPRepository;
import com.example.bookgarden.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@EnableScheduling
public class OTPService {
    private final int OTP_LENGTH = 6;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OTPRepository OTPRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    TemplateEngine templateEngine;

    public void sendRegisterOtp(String email) {
        String otp = generateOtp();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            StringBuilder mailContentBuilder = new StringBuilder();
            mailContentBuilder.append("<!DOCTYPE html>")
                    .append("<html xmlns:th=\"http://www.thymeleaf.org\">")
                    .append("<head>")
                    .append("<meta charset=\"UTF-8\">")
                    .append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">")
                    .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                    .append("<title>Email Verification</title>")
                    .append("<style>")
                    .append("body {")
                    .append("font-family: Arial, sans-serif;")
                    .append("line-height: 1.6;")
                    .append("margin: 0;")
                    .append("padding: 0;")
                    .append("}")
                    .append(".container {")
                    .append("width: 100%;")
                    .append("max-width: 600px;")
                    .append("margin: 0 auto;")
                    .append("padding: 20px;")
                    .append("}")
                    .append(".header {")
                    .append("text-align: center;")
                    .append("margin-bottom: 20px;")
                    .append("}")
                    .append(".logo {")
                    .append("max-width: 250px;")
                    .append("}")
                    .append(".message {")
                    .append("margin-bottom: 15px;")
                    .append("}")
                    .append(".otp {")
                    .append("text-align: center;")
                    .append("font-size: 40px;")
                    .append("font-weight: bold;")
                    .append("margin-bottom: 20px;")
                    .append("}")
                    .append(".verify-text {")
                    .append("text-align: left;")
                    .append("margin-bottom: 20px;")
                    .append("}")
                    .append(".verify-link {")
                    .append("display: block;")
                    .append("text-align: center;")
                    .append("text-decoration: none;")
                    .append("background-color: #18b4b2;")
                    .append("color: white;")
                    .append("padding: 12px 20px;")
                    .append("border-radius: 4px;")
                    .append("max-width: 300px;")
                    .append("margin: 0 auto;")
                    .append("border: none;")
                    .append("font-size: 16px;")
                    .append("font-weight: bold;")
                    .append("}")
                    .append(".footer {")
                    .append("margin-top: 30px;")
                    .append("}")
                    .append(".email-container {")
                    .append("border: 2px solid #ccc;")
                    .append("padding: 20px;")
                    .append("border-radius: 10px;")
                    .append("}")
                    .append("</style>")
                    .append("</head>")
                    .append("<body>")
                    .append("<div class=\"container email-container\">")
                    .append("<div class=\"header\">")
                    .append("<img class=\"logo\" src=\"https://res.cloudinary.com/dfwwu6ft4/image/upload/v1702616384/logo-home1_acmr3x.png\" alt=\"Logo\">")
                    .append("</div>")
                    .append("<div class=\"message\">")
                    .append("<p>Xin chào,</p>")
                    .append("<p>Cảm ơn bạn đã đăng ký tài khoản website Book Garden.</p>")
                    .append("<p>Mã OTP của bạn là:</p>")
                    .append("</div>")
                    .append("<div class=\"otp\">")
                    .append("<span th:text=\"${otpCode}\">").append(otp).append("</span>")
                    .append("</div>")
                    .append("<div class=\"verify-text\">")
                    .append("<p>Vui lòng nhập mã ở trên vào đường dẫn sau để xác thực tài khoản của bạn.</p>")
                    .append("<p><i>(*) Lưu ý: Mã OTP chỉ có giá trị trong vòng 5 phút.</i></p>")
                    .append("</div>")
                    .append("<a href=\"https://book-garden-reactjs.web.app/email/verify?email=").append(email).append("\" style=\"text-decoration: none;\">")
                    .append("<button class=\"verify-link\">")
                    .append("Xác thực")
                    .append("</button>")
                    .append("</a>")
                    .append("<div class=\"footer\">")
                    .append("<p>Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p>")
                    .append("</div>")
                    .append("</div>")
                    .append("</body>")
                    .append("</html>");
            helper.setText(mailContentBuilder.toString(), true);
            helper.setSubject("Mã xác thực đăng ký tài khoản");
            mailSender.send(message);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            OTP OTP = new OTP();
            OTP.setEmail(email);
            OTP.setOtp(otp);
            OTP.setExpirationTime(expirationTime);

            Optional<OTP> existingEmailVerification = findByEmail(email);
            if (existingEmailVerification.isPresent()) {
                OTPRepository.delete(existingEmailVerification.get());
            }

            OTPRepository.save(OTP);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<OTP> emailVerification = OTPRepository.findByEmail(email);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent() && emailVerification.isPresent() && emailVerification.get().getOtp().equals(otp)) {
            User user = optionalUser.get();
            user.setIsVerified(true);
            user.setIsActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public void sendForgotPasswordOtp(String email) {
            String otp = generateOtp();
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(email);

                StringBuilder mailContentBuilder = new StringBuilder();
                mailContentBuilder.append("<!DOCTYPE html>")
                        .append("<html xmlns:th=\"http://www.thymeleaf.org\">")
                        .append("<head>")
                        .append("<meta charset=\"UTF-8\">")
                        .append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">")
                        .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                        .append("<title>Email Verification</title>")
                        .append("<style>")
                        .append("/* CSS styles here */")
                        .append("</style>")
                        .append("</head>")
                        .append("<body>")
                        .append("<div class=\"container email-container\">")
                        .append("<div class=\"header\">")
                        .append("<img class=\"logo\" src=\"https://res.cloudinary.com/dfwwu6ft4/image/upload/v1702616384/logo-home1_acmr3x.png\" alt=\"Logo\">")
                        .append("</div>")
                        .append("<div class=\"message\">")
                        .append("<p>Xin chào,</p>")
                        .append("<p>Bạn đang thực hiện Khôi phục mật khẩu trên website Book Garden.</p>")
                        .append("<p>Mã OTP của bạn là:</p>")
                        .append("</div>")
                        .append("<div class=\"otp\">")
                        .append("<span>").append(otp).append("</span>")
                        .append("</div>")
                        .append("<div class=\"verify-text\">")
                        .append("<p>Vui lòng nhập mã ở trên vào đường dẫn sau để xác thực tài khoản của bạn.</p>")
                        .append("<p><i>(*) Lưu ý: Mã OTP chỉ có giá trị trong vòng 5 phút.</i></p>")
                        .append("</div>")
                        .append("<a href=\"https://book-garden-reactjs.web.app/email/verify?email=").append(email).append("\" style=\"text-decoration: none;\">")
                        .append("<button class=\"verify-link\">")
                        .append("Xác thực")
                        .append("</button>")
                        .append("</a>")
                        .append("<div class=\"footer\">")
                        .append("<p>Nếu không phải bạn không đang thực hiện chức năng, vui lòng bỏ qua email này.</p>")
                        .append("</div>")
                        .append("</div>")
                        .append("</body>")
                        .append("</html>");

                helper.setText(mailContentBuilder.toString(), true);
                helper.setSubject("Reset Password OTP");
                mailSender.send(message);

                LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
                OTP otpObject = new OTP();
                otpObject.setEmail(email);
                otpObject.setOtp(otp);
                otpObject.setExpirationTime(expirationTime);

                Optional<OTP> existingOtp = findByEmail(email);
                if (existingOtp.isPresent()) {
                    OTPRepository.delete(existingOtp.get());
                }

                OTPRepository.save(otpObject);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    public Optional<OTP> findByEmail(String email) {
        return OTPRepository.findByEmail(email);
    }

    public void deleteExpiredOtp() {
        LocalDateTime now = LocalDateTime.now();
        List<OTP> expiredOtpList = OTPRepository.findByExpirationTimeBefore(now);
        OTPRepository.deleteAll(expiredOtpList);
    }
    @Scheduled(fixedDelay = 30000) // 5 minutes
    public void cleanupExpiredOtp() {
        deleteExpiredOtp();
    }
}
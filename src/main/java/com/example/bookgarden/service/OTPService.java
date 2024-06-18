package com.example.bookgarden.service;

import com.example.bookgarden.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.bookgarden.entity.OTP;
import com.example.bookgarden.repository.OTPRepository;
import com.example.bookgarden.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@EnableScheduling
public class OTPService {
    private static final int OTP_LENGTH = 6;
    private static final String REGISTER_SUBJECT = "Mã xác thực đăng ký tài khoản";
    private static final String FORGOT_PASSWORD_SUBJECT = "Reset Password OTP";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPRepository otpRepository;

    public void sendRegisterOtp(String email) {
        String otp = generateOtp();
        try {
            sendOtpEmail(email, otp, REGISTER_SUBJECT, buildEmailRegisterOTPContent(email, otp));
            saveOtp(email, otp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending OTP", e);
        }
    }

    public void sendForgotPasswordOtp(String email) {
        String otp = generateOtp();
        try {
            sendOtpEmail(email, otp, FORGOT_PASSWORD_SUBJECT, buildForgotPasswordEmailContent(email, otp));
            saveOtp(email, otp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending OTP", e);
        }
    }

    private void sendOtpEmail(String email, String otp, String subject, String content) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    protected  String generateOtp() {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String buildEmailRegisterOTPContent(String email, String otp) {
        return new StringBuilder()
                .append("<!DOCTYPE html>")
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
                .append("<p>Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>")
                .toString();
    }

    private String buildForgotPasswordEmailContent(String email, String otp) {
        return new StringBuilder()
                .append("<!DOCTYPE html>")
                .append("<html xmlns:th=\"http://www.thymeleaf.org\">")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>Reset Password OTP</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; }")
                .append(".container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append(".header { text-align: center; margin-bottom: 20px; }")
                .append(".logo { max-width: 250px; }")
                .append(".message { margin-bottom: 15px; }")
                .append(".otp { text-align: center; font-size: 40px; font-weight: bold; margin-bottom: 20px; }")
                .append(".verify-text { text-align: left; margin-bottom: 20px; }")
                .append(".verify-link { display: block; text-align: center; text-decoration: none; background-color: #18b4b2; color: white; padding: 12px 20px; border-radius: 4px; max-width: 300px; margin: 0 auto; border: none; font-size: 16px; font-weight: bold; }")
                .append(".footer { margin-top: 30px; }")
                .append(".email-container { border: 2px solid #ccc; padding: 20px; border-radius: 10px; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container email-container\">")
                .append("<div class=\"header\"><img class=\"logo\" src=\"https://res.cloudinary.com/dfwwu6ft4/image/upload/v1702616384/logo-home1_acmr3x.png\" alt=\"Logo\"></div>")
                .append("<div class=\"message\"><p>Xin chào,</p><p>Bạn đang thực hiện Khôi phục mật khẩu trên website Book Garden.</p><p>Mã OTP của bạn là:</p></div>")
                .append("<div class=\"otp\"><span>").append(otp).append("</span></div>")
                .append("<div class=\"verify-text\"><p>Vui lòng nhập mã ở trên vào đường dẫn sau để xác thực tài khoản của bạn.</p><p><i>(*) Lưu ý: Mã OTP chỉ có giá trị trong vòng 5 phút.</i></p></div>")
                .append("<a href=\"https://book-garden-reactjs.web.app/email/verify?email=").append(email).append("\" style=\"text-decoration: none;\"><button class=\"verify-link\">Xác thực</button></a>")
                .append("<div class=\"footer\"><p>Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p></div>")
                .append("</div>")
                .append("</body>")
                .append("</html>")
                .toString();
    }

    protected void saveOtp(String email, String otp) {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        OTP otpEntity = new OTP(email, otp, expirationTime);

        Optional<OTP> existingOtp = otpRepository.findByEmail(email);
        existingOtp.ifPresent(otpRepository::delete);

        otpRepository.save(otpEntity);
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<OTP> emailVerification = otpRepository.findByEmail(email);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent() && emailVerification.isPresent()) {
            OTP otpEntity = emailVerification.get();
            if (otpEntity.getOtp().equals(otp) && otpEntity.getExpirationTime().isAfter(LocalDateTime.now())) {
                User user = optionalUser.get();
                user.setIsVerified(true);
                user.setIsActive(true);
                userRepository.save(user);
                otpRepository.delete(otpEntity);
                return true;
            }
        }
        return false;
    }

    public void deleteExpiredOtp() {
        LocalDateTime now = LocalDateTime.now();
        List<OTP> expiredOtpList = otpRepository.findByExpirationTimeBefore(now);
        otpRepository.deleteAll(expiredOtpList);
    }

    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void cleanupExpiredOtp() {
        deleteExpiredOtp();
    }
}

package com.example.bookgarden.controller;

import com.example.bookgarden.config.Config;
import com.example.bookgarden.dto.GenericResponse;
import com.example.bookgarden.dto.PaymentCallBackRequestDTO;
import com.example.bookgarden.dto.PaymentRequestDTO;
import com.example.bookgarden.dto.PaymentResponseDTO;
import com.example.bookgarden.entity.Order;
import com.example.bookgarden.repository.OrderRepository;
import com.example.bookgarden.security.JwtTokenProvider;
import com.example.bookgarden.service.OrderService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/v1/customer/orders/payment")
public class PaymentController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createPayment(@RequestHeader("Authorization") String authorizationHeader, @RequestBody PaymentRequestDTO paymentRequestDTO){
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        double amountReq = paymentRequestDTO.getAmount();
        long amount = (long) (amountReq * 100);
        String bankCode = "NCB";

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";

        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+0"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            cld.add(Calendar.HOUR_OF_DAY, 7);
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;


        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setStatus("Success");
        paymentResponseDTO.setMessage("Thành công");
        paymentResponseDTO.setURL(paymentUrl);
        System.out.println("vnp_ReturnUrl: " + Config.vnp_ReturnUrl);
        return ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Tạo Thanh toán thành công")
                .data(paymentResponseDTO)
                .build());
    }
    @GetMapping("/callback")
    public ResponseEntity<GenericResponse> paymentCallback(@RequestParam (value = "vnp_ResponseCode") String responseCode){
        if(responseCode.equals("00")){
            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Thanh toán thành công")
                    .data("Response Code:" + responseCode)
                    .build());
        }
        return ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Thanh toán thất bại")
                .data("Response Code:" + responseCode)
                .build());
    }
    @PostMapping("/checkout")
    public ResponseEntity<GenericResponse> handlePaymentCallback(@RequestBody PaymentCallBackRequestDTO paymentCallBackRequestDTO){
        return orderService.handlePaymentCallback(paymentCallBackRequestDTO);
    }
}

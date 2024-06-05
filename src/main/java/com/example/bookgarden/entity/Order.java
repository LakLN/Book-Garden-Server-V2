package com.example.bookgarden.entity;

import com.example.bookgarden.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    @Id
    private ObjectId id;
    private ObjectId user;
    private String fullName;
    private String phone;
    private String address;
    private List<ObjectId> orderItems;
    private double totalAmount;
    private Date orderDate = new Date();
    private OrderStatus status;
    private String paymentMethod = "ON_DELIVERY";
    private Date paymentDate;
    private double paymentAmount;
    private String paymentStatus = "NOT_PAID";
    public void setStatus(String status) {
        this.status = OrderStatus.fromString(status);
    }
}

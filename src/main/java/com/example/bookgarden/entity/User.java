package com.example.bookgarden.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    private String id;
    private String fullName;
    private String passWord;
    private String email;
    private String role;
    private String phone;
    private Boolean isActive = false;
    private Boolean isVerified = false;
    private Date createdAt = new Date();
    @LastModifiedDate
    private Date updatedAt;
    @Field(targetType = FieldType.STRING)
    private String gender;
    private String avatar = "https://res.cloudinary.com/dfwwu6ft4/image/upload/v1702295984/z4964484552810_6883dabc5756c5535dba610f6492807c_wna4vw.jpg";
    private Date birthday;
    private List<String> addresses = new ArrayList<>();
    private String cart;
    private List<ObjectId> historyOrders = new ArrayList<>();
    private String wishList;
    private List<ObjectId> posts = new ArrayList<>();
    private float points;
}


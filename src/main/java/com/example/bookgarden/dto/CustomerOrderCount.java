package com.example.bookgarden.dto;

import org.bson.types.ObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderCount {
    @Field("_id")
    private ObjectId userId;
    private long orderCount;
}
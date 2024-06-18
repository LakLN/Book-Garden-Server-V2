package com.example.bookgarden.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericResponse {
    private boolean success;
    private String message;
    private Object data;
}

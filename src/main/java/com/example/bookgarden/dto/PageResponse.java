package com.example.bookgarden.dto;
import lombok.Data;

import java.util.List;
@Data
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

}

package com.example.bookgarden.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;


@Document(collection = "search_histories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistory {

    @Id
    private String id;
    private String userId;
    private String searchQuery;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime searchDate;
    public SearchHistory(String userId, String searchQuery) {
        this.userId = userId;
        this.searchQuery = searchQuery;
        this.searchDate = LocalDateTime.now();
    }

}
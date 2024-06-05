package com.example.bookgarden.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Data
public class StatisticDTO {
    private long bookCount;
    private long userCount;
    private long postCount;
    private long categoryCount;
    private long authorCount;
    private long commentCount;
    private long orderCount;
    private double totalRevenue;
    private Map<LocalDate, Double> revenueByDay;
    private Map<YearMonth, Double> revenueByMonth;
    private Map<Integer, Double> revenueByYear;
}

package com.engineering.orgcore.dto.dashboard;

public record MonthlySeriesDto(
        int year,
        int month,
        String monthLabel,
        Double totalAmount,
        Long orderCount,
        Long individualCount
        ) {}

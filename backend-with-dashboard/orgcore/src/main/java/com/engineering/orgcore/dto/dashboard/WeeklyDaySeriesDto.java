package com.engineering.orgcore.dto.dashboard;

public record WeeklyDaySeriesDto(
        Number dayOfWeek,       // 1=Monday ... 7=Sunday (ISO)
        String dayLabel,
        Double totalAmount,
        Integer orderCount,
        Integer individualCount
) {}

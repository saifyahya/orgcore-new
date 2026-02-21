package com.engineering.orgcore.dto.dashboard;

import java.time.LocalDate;

public record WeeklyDaySeriesDto(
        LocalDate saleDate,
        Number dayOfWeek,       // 1=Monday ... 7=Sunday (ISO)
        String dayLabel,
        Double totalAmount,
        Integer orderCount,
        Integer individualCount
) {}

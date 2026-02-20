package com.engineering.orgcore.dto.dashboard;

public record CategorySalesDto(
        Long categoryId,
        String categoryName,
        String categoryImage,
        Long totalQuantity,
        Double totalRevenue,
        Long orderCount
) {}

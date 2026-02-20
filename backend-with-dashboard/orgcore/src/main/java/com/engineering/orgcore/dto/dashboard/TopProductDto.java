package com.engineering.orgcore.dto.dashboard;

public record TopProductDto(
        Long productId,
        String productName,
        String productImage,
        Long totalQuantity,
        Double totalRevenue
) {}

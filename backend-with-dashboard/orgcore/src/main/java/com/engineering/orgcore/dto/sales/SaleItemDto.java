package com.engineering.orgcore.dto.sales;

public record SaleItemDto(
        Long productId,
        String code,
        String name,
        Integer quantity,
        Double unitPrice
) {}

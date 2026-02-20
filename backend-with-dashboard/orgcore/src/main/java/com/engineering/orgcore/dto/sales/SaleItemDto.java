package com.engineering.orgcore.dto.sales;

public record SaleItemDto(
        Long productId,
        Integer quantity,
        Double unitPrice
) {}

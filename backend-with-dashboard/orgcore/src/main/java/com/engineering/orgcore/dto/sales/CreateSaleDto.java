package com.engineering.orgcore.dto.sales;

import java.util.List;

public record CreateSaleDto(
        Long branchId,
        Double totalAmount,
        Double discountAmount,
        Double taxAmount,
        String paymentMethod,
        String channel,
        String externalRef,
        List<SaleItemDto> items) {
}

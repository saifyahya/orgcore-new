package com.engineering.orgcore.dto.sales;

import com.engineering.orgcore.util.ExcelIndex;

import java.util.List;

public record CreateSaleDto(
       @ExcelIndex(0) Long branchId,
       @ExcelIndex(1)   Double totalAmount,
       @ExcelIndex(2)  Double discountAmount,
       @ExcelIndex(3)  Double taxAmount,
       @ExcelIndex(4)  String paymentMethod,
       @ExcelIndex(5)  String channel,
       @ExcelIndex(6)  String externalRef,
        List<SaleItemDto> items) {
}

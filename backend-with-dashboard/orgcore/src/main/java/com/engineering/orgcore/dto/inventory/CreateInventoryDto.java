package com.engineering.orgcore.dto.inventory;

import com.engineering.orgcore.util.ExcelIndex;

public record CreateInventoryDto(
    @ExcelIndex(0) Long branchId ,
    @ExcelIndex(1) String productCode,
    @ExcelIndex(2) Long quantity,
    @ExcelIndex(3) String note,
    @ExcelIndex(4) String referenceType) {
}

package com.engineering.orgcore.dto.sales;

import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.dto.product.ProductDto;
import com.engineering.orgcore.enums.ReferenceType;
import com.engineering.orgcore.enums.StockMovementReason;
import com.engineering.orgcore.enums.StockMovementType;

import java.util.UUID;

public record StockMovementDto(
        Long id,
        BranchDto branch,
        ProductDto product,
        StockMovementType type,
        StockMovementReason reason,
        Integer quantity,
        Double unitCost,
        ReferenceType refType,
        String refId,
        String note
) {}

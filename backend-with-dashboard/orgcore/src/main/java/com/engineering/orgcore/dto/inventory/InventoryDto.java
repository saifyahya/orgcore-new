package com.engineering.orgcore.dto.inventory;

import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.dto.product.ProductDto;

public record InventoryDto(
        Long id,
        BranchDto branch,
        ProductDto product,
        Long quantity,
        String createdAt,
        String createdBy,
        String updatedAt,
        String updatedBy) {}

package com.engineering.orgcore.dto.sales;

import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.enums.PaymentMethod;
import com.engineering.orgcore.enums.SaleChannel;

import java.util.List;

public record SaleDto(
        Long id,
        BranchDto branch,
        Double totalAmount,
        Double discountRate,
        Double taxRate,
        Double finalAmount,
        PaymentMethod paymentMethod,
        SaleChannel channel,
        String externalRef,
        List<SaleItemDto> items,
        String createdAt,
        String createdBy,
        String updatedAt,
        String updatedBy
) {}

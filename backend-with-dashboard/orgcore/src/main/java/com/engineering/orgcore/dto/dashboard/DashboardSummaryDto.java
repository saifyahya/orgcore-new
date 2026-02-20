package com.engineering.orgcore.dto.dashboard;

public record DashboardSummaryDto(
        Double totalSalesAmount,
        Double totalDiscountAmount,
        Double totalTaxAmount,
        Double finalAmount,
        Long totalOrders,
        Double avgOrderValue,
        Double avgSalesPerProduct,
        Long totalProducts,
        Long totalCategories,
        Long activeBranches
) {}

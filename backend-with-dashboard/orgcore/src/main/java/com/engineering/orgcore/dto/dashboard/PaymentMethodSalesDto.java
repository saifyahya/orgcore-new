package com.engineering.orgcore.dto.dashboard;

public record PaymentMethodSalesDto(
        String paymentMethod,
        Long orderCount,
        Double totalAmount
) {}

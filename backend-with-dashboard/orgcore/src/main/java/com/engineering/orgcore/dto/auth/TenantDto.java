package com.engineering.orgcore.dto.auth;

public record TenantDto(
        String tenantName,
        String address,
        String email,
        String phone
) {}

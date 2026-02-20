package com.engineering.orgcore.dto.auth;

public record AuthRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        TenantDto tenant
) {}

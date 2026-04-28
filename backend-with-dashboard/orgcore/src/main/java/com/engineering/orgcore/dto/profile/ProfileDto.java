package com.engineering.orgcore.dto.profile;

public record ProfileDto(
        // User fields
        Long userId,
        String firstName,
        String lastName,
        String email,
        Integer userIsActive,

        // Tenant fields
        Long tenantId,
        String tenantName,
        String address,
        String phone,
        Integer tenantIsActive,

        // Metadata
        String createdBy,
        String createdAt,
        String updatedBy,
        String updatedAt
) {}


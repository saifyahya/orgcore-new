package com.engineering.orgcore.dto.category;

public record CategoryDto(
        Long id,
        String name,
        String image,
        Integer isActive,
        String createdBy,
        String createdAt,
        String updatedBy,
        String updatedAt
) {}

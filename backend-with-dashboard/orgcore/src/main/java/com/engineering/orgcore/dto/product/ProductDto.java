package com.engineering.orgcore.dto.product;

import com.engineering.orgcore.dto.category.CategoryDto;

public record ProductDto(
        Long id,
        String name,
        String description,
        CategoryDto categoryDto,
        String image,
        Double price,
        Integer isActive,
        String createdBy,
        String createdAt,
        String updatedBy,
        String updatedAt
) {}

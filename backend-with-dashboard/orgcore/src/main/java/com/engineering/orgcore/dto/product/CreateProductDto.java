package com.engineering.orgcore.dto.product;
import com.engineering.orgcore.dto.category.CategoryDto;

public record CreateProductDto(
        Long id,
        String name,
        String description,
        Long categoryId,
        String image,
        Double price,
        Double discount,
        Integer isActive,
        Long rate,
        String createdBy,
        String createdAt,
        String updatedBy,
        String updatedAt
) {}

package com.engineering.orgcore.dto.product;
import com.engineering.orgcore.dto.category.CategoryDto;
import com.engineering.orgcore.util.ExcelIndex;

public record  CreateProductDto(
       @ExcelIndex(0) String name,
       @ExcelIndex(1) String description,
       @ExcelIndex(2) Long categoryId,
       @ExcelIndex(3) Double price,
       @ExcelIndex(4) Integer isActive,
       @ExcelIndex(5) String code
) {}

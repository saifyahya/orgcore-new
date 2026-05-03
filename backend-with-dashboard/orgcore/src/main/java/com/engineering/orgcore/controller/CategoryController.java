package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.category.CategoryDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final Utils utils;


    // Create
    @PostMapping
    public CategoryDto create(
            @Valid @RequestBody CategoryDto request
    ) {
        return categoryService.create(utils.getCurrentTenant(),request);
    }

    // Get by id
    @GetMapping("/{id}")
    public CategoryDto getById(
            @PathVariable Long id
    ) throws NotFoundException {
        return categoryService.getById(utils.getCurrentTenant(), id);
    }

    // Get all (paged)
    // Example:
    // GET /api/categories?tenantId=1&page=0&size=20&sortBy=id&sortDir=asc&q=drinks
    @GetMapping
    public Page<CategoryDto> getAll(
            @ModelAttribute PageFilter pageFilter
    ) {
        return categoryService.getAll(utils.getCurrentTenant(), pageFilter);
    }

    // Update
    @PutMapping("/{id}")
    public CategoryDto update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto request
    ) throws NotFoundException {
        return categoryService.update(utils.getCurrentTenant(),id, request);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) throws NotFoundException {
        categoryService.delete(utils.getCurrentTenant(), id);
    }
}

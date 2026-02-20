package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.product.CreateProductDto;
import com.engineering.orgcore.dto.product.ProductDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final Utils utils;

    // Create
    @PostMapping
    public ProductDto create(
            @Valid @RequestBody CreateProductDto request
    ) throws NotFoundException {
        return productService.create(utils.getCurrentTenant(), request);
    }

    // Get by id
    @GetMapping("/{id}")
    public ProductDto getById(
            @PathVariable Long id
    ) throws NotFoundException {
        return productService.getById(id);
    }

    // Get all (paged + optional search)
    // Example:
    // GET /api/products?tenantId=1&page=0&size=20&sortBy=id&sortDir=asc&q=burger
    @GetMapping
    public Page<ProductDto> getAll(
            @ModelAttribute PageFilter pageFilter
    ) {
        return productService.getAll(utils.getCurrentTenant(), pageFilter);
    }

    // Update
    @PutMapping("/{id}")
    public ProductDto update(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductDto request
    ) throws NotFoundException {
        return productService.update(utils.getCurrentTenant(), id, request);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) throws NotFoundException {
        productService.
                delete(utils.getCurrentTenant(), id);
    }
}

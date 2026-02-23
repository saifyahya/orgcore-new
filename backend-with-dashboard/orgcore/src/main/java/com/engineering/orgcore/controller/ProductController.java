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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final Utils utils;

    // Create
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDto create(
            @Valid @ModelAttribute CreateProductDto request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws NotFoundException {
        return productService.create(utils.getCurrentTenant(), request, imageFile);
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
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDto update(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateProductDto request,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws NotFoundException {
        return productService.update(utils.getCurrentTenant(), id, request, imageFile);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) throws NotFoundException {
        productService.
                delete(utils.getCurrentTenant(), id);
    }


    @PostMapping("/import")
    public ResponseEntity<String> importInventory(
            @RequestParam("file") MultipartFile file) throws Exception{
        return ResponseEntity.ok(productService.importInventory(file, utils.getCurrentTenant()));
    }
}

package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.product.CreateProductDto;
import com.engineering.orgcore.dto.product.ProductDto;
import com.engineering.orgcore.entity.Category;
import com.engineering.orgcore.entity.Product;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.CategoryRepository;
import com.engineering.orgcore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@CrossOrigin
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Utils utils;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;


    public ProductDto create(Long tenantId, CreateProductDto request, MultipartFile imageFile) throws NotFoundException {

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.categoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        if (productRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.name().trim())) {
            throw new IllegalArgumentException("Product name already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));

        Product product = new Product();
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(category);

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile);
            product.setImage(imagePath);
        }

        product.setPrice(request.price());
        product.setIsActive(request.isActive() != null ? request.isActive() : 1);
        product.setTenantId(tenantId);

        product.setCreatedBy(utils.getCurrentUserName());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedBy(utils.getCurrentUserName());
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ProductDto getById(Long id) throws NotFoundException {
        Long tenantId = utils.getCurrentTenant();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        if (!tenantId.equals(product.getTenantId())) {
            throw new NotFoundException("Product not found with id: " + id);
        }

        return toDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getAll(Long tenantId, PageFilter pageFilter) {
        Page<Product> page = productRepository.findAllByTenantId(
                    tenantId,
                    pageFilter.getSearch(),
                    pageFilter.getCategoryId(),
                    pageFilter.getIsActive(),
                    pageFilter.toPageable()
            );

        return page.map(this::toDto);
    }

    public ProductDto update(Long tenantId, Long id, CreateProductDto request, MultipartFile imageFile) throws NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        if (!tenantId.equals(product.getTenantId())) {
            throw new NotFoundException("Product not found with id: " + id);
        }

        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name().trim());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));
            product.setCategory(category);
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                fileStorageService.deleteFile(product.getImage());
            }
            // Store new image
            String imagePath = fileStorageService.storeFile(imageFile);
            product.setImage(imagePath);
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }


        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }


        product.setUpdatedBy(utils.getCurrentUserName());
        product.setUpdatedAt(LocalDateTime.now());
        return toDto(product);
    }

    public void delete(Long tenantId, Long id) throws NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        if (!tenantId.equals(product.getTenantId())) {
            throw new NotFoundException("Product not found with id: " + id);
        }

        if (product.getInventories() != null && !product.getInventories().isEmpty()) {
            throw new IllegalStateException("Cannot delete product because it has inventories.");
        }
        if (product.getSaleItems() != null && !product.getSaleItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete product because it has sale items.");
        }

        // Delete image file if exists
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            fileStorageService.deleteFile(product.getImage());
        }

        product.setIsActive(0);
    }

    public ProductDto toDto(Product p) {
        String encryptedImage = (p.getImage());
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                categoryService.toDto(p.getCategory()),
                encryptedImage,
                p.getPrice(),
                p.getIsActive(),
                p.getCreatedBy(),
                p.getCreatedAt().toString(),
                p.getUpdatedBy(),
                p.getUpdatedAt().toString()
        );
    }
}

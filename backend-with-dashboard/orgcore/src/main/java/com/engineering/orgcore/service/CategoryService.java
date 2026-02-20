package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.category.CategoryDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.entity.Category;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final Utils utils;


    public CategoryDto create(Long tenantId, CategoryDto request) {

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        if (categoryRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.name().trim())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = new Category();
        category.setName(request.name().trim());
        category.setImage(request.image());
        category.setIsActive(request.isActive() != null ? request.isActive() : 1);
        category.setTenantId(tenantId);
        category.setCreatedBy(utils.getCurrentUserName());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedBy(utils.getCurrentUserName());
        category.setUpdatedAt(LocalDateTime.now());

        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long tenantId, Long id) throws NotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        if (!tenantId.equals(category.getTenantId())) {
            throw new NotFoundException("Category not found with id: " + id);
        }

        return toDto(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> getAll(Long tenantId, PageFilter pageFilter) {
        Page<Category> page = categoryRepository.findAllByTenantId(
                    tenantId,
                    pageFilter.getSearch(),
                    pageFilter.getIsActive(),
                    pageFilter.toPageable()
            );

        return page.map(this::toDto);
    }

    public CategoryDto update(Long tenantId, Long id, CategoryDto request) throws NotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        if (!tenantId.equals(category.getTenantId())) {
            throw new NotFoundException("Category not found with id: " + id);
        }

        if (request.name() != null && !request.name().isBlank()) {
            category.setName(request.name().trim());
        }

        if (request.image() != null) {
            category.setImage(request.image());
        }

        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }

        category.setUpdatedBy(utils.getCurrentUserName());
        category.setUpdatedAt(LocalDateTime.now());

        return toDto(category);
    }

    public void delete(Long tenantId, Long id) throws NotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        if (!tenantId.equals(category.getTenantId())) {
            throw new NotFoundException("Category not found with id: " + id);
        }

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category because it has products.");
        }

        category.setIsActive(0);
    }

    public CategoryDto toDto(Category c) {
        return new CategoryDto(
                c.getId(),
                c.getName(),
                c.getImage(),
                c.getIsActive(),
                c.getCreatedBy(),
                c.getCreatedAt().toString(),
                c.getUpdatedBy(),
                c.getUpdatedAt().toString()
        );
    }
}

package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.inventory.InventoryDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;
    private final Utils utils;

    // Create
    @PostMapping
    public InventoryDto create(
            @Valid @RequestBody InventoryDto request
    ) throws NotFoundException {
        return inventoryService.create(utils.getCurrentTenant(), request);
    }

    // Get by id
    @GetMapping("/{id}")
    public InventoryDto getById(
            @PathVariable Long id
    ) throws NotFoundException {
        return inventoryService.getById(utils.getCurrentTenant(), id);
    }

    // Get all (paged) with optional filters:
    // /api/inventories?tenantId=1&page=0&size=20&branchId=10&productId=99
    @GetMapping
    public Page<InventoryDto> getAll(
            @ModelAttribute PageFilter pageFilter,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long productId
    ) {
        return inventoryService.getAll(utils.getCurrentTenant(), pageFilter, branchId, productId);
    }

    // Update (quantity)
    @PutMapping("/{id}")
    public InventoryDto update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDto request
    ) throws NotFoundException {
        return inventoryService.update(utils.getCurrentTenant(), id, request);
    }

    // Delete
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) throws NotFoundException {
        inventoryService.delete(utils.getCurrentTenant(), id);
    }
}

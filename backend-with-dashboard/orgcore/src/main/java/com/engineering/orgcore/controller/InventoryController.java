package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.inventory.CreateInventoryDto;
import com.engineering.orgcore.dto.inventory.InventoryDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.InventoryService;
import com.engineering.orgcore.util.ExcelParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final Utils utils;

    // Create
    @PostMapping
    public InventoryDto create(
            @Valid @RequestBody CreateInventoryDto request
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
            @RequestParam(required = false) Long branchId) {
        return inventoryService.getAll(utils.getCurrentTenant(), branchId, pageFilter);
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

    @PostMapping("/import")
    public ResponseEntity<String> importInventory(
            @RequestParam("file") MultipartFile file) throws Exception{
            return ResponseEntity.ok(inventoryService.importInventory(file, utils.getCurrentTenant()));
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @ModelAttribute PageFilter pageFilter,
            @RequestParam(required = false) Long branchId) {
        byte[] excelBytes = inventoryService.exportToExcel(utils.getCurrentTenant(), branchId, pageFilter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}

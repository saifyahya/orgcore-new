package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.sales.StockMovementDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final Utils utils;

    @GetMapping
    public Page<StockMovementDto> getAll(
            @ModelAttribute PageFilter pageFilter,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long productId
    ) {
        return stockMovementService.getAll(utils.getCurrentTenant(), pageFilter, branchId, productId);
    }

    @GetMapping("/{id}")
    public StockMovementDto getById(
            @PathVariable Long id
    ) throws NotFoundException {
        return stockMovementService.getById(utils.getCurrentTenant(), id);
    }

    // Optional: manual admin adjustment
    @PostMapping("/manual-adjustment")
    public StockMovementDto createManual(
            @RequestBody StockMovementDto request
    ) {
        return stockMovementService.createManualAdjustment(utils.getCurrentTenant(), request);
    }
}

package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.sales.SaleDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;
    private final Utils utils;

    @PostMapping
    public SaleDto create(
            @RequestBody SaleDto request
    ) throws NotFoundException {
        return saleService.create(utils.getCurrentTenant(), request);
    }

    @GetMapping("/{id}")
    public SaleDto getById(
            @PathVariable Long id
    ) throws NotFoundException {
        return saleService.getById(utils.getCurrentTenant(), id);
    }

    @GetMapping
    public Page<SaleDto> getAll(
            @ModelAttribute PageFilter pageFilter
    ) {
        return saleService.getAll(utils.getCurrentTenant(), pageFilter);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) throws NotFoundException {
        saleService.delete(utils.getCurrentTenant(), id);
    }
}

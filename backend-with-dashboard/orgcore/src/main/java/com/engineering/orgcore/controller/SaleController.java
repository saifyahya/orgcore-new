package com.engineering.orgcore.controller;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.sales.CreateSaleDto;
import com.engineering.orgcore.dto.sales.SaleDto;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final Utils utils;

    @PostMapping
    public SaleDto create(
            @RequestBody CreateSaleDto request
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
            @ModelAttribute PageFilter pageFilter,
            @RequestParam(required = false) Long branchId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
            ) {
        return saleService.getAll(utils.getCurrentTenant(), branchId, startDate, endDate, pageFilter);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) throws NotFoundException {
        saleService.delete(utils.getCurrentTenant(), id);
    }


    @PostMapping("/import")
    public ResponseEntity<String> importSale(
            @RequestParam("file") MultipartFile file) throws Exception{
        return ResponseEntity.ok(saleService.importSales(file, utils.getCurrentTenant()));
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportSalePdf(
            @PathVariable Long id) throws NotFoundException, IOException {
        byte[] pdf = saleService.exportSalePdf(utils.getCurrentTenant(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sale-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @ModelAttribute PageFilter pageFilter,
            @RequestParam(required = false) Long branchId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        byte[] excelBytes = saleService.exportToExcel(utils.getCurrentTenant(), branchId, startDate, endDate, pageFilter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}

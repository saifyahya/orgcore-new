package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.inventory.CreateInventoryDto;
import com.engineering.orgcore.dto.inventory.InventoryDto;
import com.engineering.orgcore.entity.Branch;
import com.engineering.orgcore.entity.Inventory;
import com.engineering.orgcore.entity.Product;
import com.engineering.orgcore.entity.StockMovement;
import com.engineering.orgcore.enums.ReferenceType;
import com.engineering.orgcore.enums.StockMovementReason;
import com.engineering.orgcore.enums.StockMovementType;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.BranchRepository;
import com.engineering.orgcore.repository.InventoryRepository;
import com.engineering.orgcore.repository.ProductRepository;
import com.engineering.orgcore.repository.StockMovementRepository;
import com.engineering.orgcore.util.ExcelParserService;
import jdk.jshell.execution.Util;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;
    private final BranchService branchService;
    private final ExcelParserService excelParserService;
    private final Utils utils;


    public InventoryDto create(Long tenantId, CreateInventoryDto request) throws NotFoundException {

        if (request.branchId() == null) {
            throw new IllegalArgumentException("branchId is required");
        }
        if (request.productCode() == null) {
            throw new IllegalArgumentException("productCode is required");
        }
        if (request.quantity() == null || request.quantity() < 0) {
            throw new IllegalArgumentException("quantity must be >= 0");
        }
        if (request.quantity() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("quantity is too large");
        }

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + request.branchId()));
        Product product = productRepository.findByCode(request.productCode())
                .orElseThrow(() -> new NotFoundException("Product not found with code: " + request.productCode()));

        if (!tenantId.equals(branch.getTenantId())) {
            throw new NotFoundException("Branch not found with id: " + request.branchId());
        }
        if (!tenantId.equals(product.getTenantId())) {
            throw new NotFoundException("Product not found with code: " + request.productCode());
        }

        inventoryRepository.findByTenantIdAndBranch_IdAndProduct_Code(tenantId, request.branchId(), request.productCode())
                .ifPresent(inv -> {
                    throw new IllegalArgumentException("Inventory already exists for this branch and product. Use update.");
                });

        Inventory inv = new Inventory();
        inv.setBranch(branch);
        inv.setProduct(product);
        inv.setQuantity(request.quantity());
        inv.setTenantId(tenantId);
        inv.setCreatedBy(utils.getCurrentUserName());
        inv.setCreatedAt(LocalDateTime.now());
        inv.setUpdatedBy(utils.getCurrentUserName());
        inv.setUpdatedAt(LocalDateTime.now());
        Inventory saved = inventoryRepository.save(inv);

        // ✅ Stock movement audit (initial stock / manual add)
        if (request.quantity() > 0) {
            StockMovement sm = new StockMovement();
            sm.setTenantId(tenantId);
            sm.setBranchId(branch.getId());
            sm.setProductId(product.getId());
            sm.setType(StockMovementType.IN);
            sm.setReason(StockMovementReason.PURCHASE); // or MANUAL / PURCHASE / INITIAL
            sm.setQuantity(request.quantity().intValue());
            sm.setUnitCost(null);
            sm.setRefType(ReferenceType.fromValue(request.referenceType()));     // or IMPORT if created via import
            sm.setRefId(null);
            sm.setNote(request.note());

            stockMovementRepository.save(sm);
        }

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public InventoryDto getById(Long tenantId, Long id) throws NotFoundException {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory not found with id: " + id));

        if (!tenantId.equals(inv.getTenantId())) {
            throw new NotFoundException("Inventory not found with id: " + id);
        }

        return toDto(inv);
    }

    @Transactional(readOnly = true)
    public Page<InventoryDto> getAll(Long tenantId, Long branchId, PageFilter pageFilter) {
        Page<Inventory> page = inventoryRepository.findAllByTenantId(tenantId, branchId, pageFilter.getSearch(), pageFilter.toPageable());
        return page.map(this::toDto);
    }

    public InventoryDto update(Long tenantId, Long id, InventoryDto request) throws NotFoundException {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory not found with id: " + id));

        if (!tenantId.equals(inv.getTenantId())) {
            throw new NotFoundException("Inventory not found with id: " + id);
        }

        // Usually inventory update = update quantity only
        if (request.quantity() != null) {
            if (request.quantity() < 0) {
                throw new IllegalArgumentException("quantity must be >= 0");
            }
            inv.setQuantity(request.quantity());
        }

        // If you want to allow changing branch/product, it becomes more complex because of uniqueness.
        // Best practice: do NOT allow changing branch/product on inventory row.
        return toDto(inv);
    }

    public void delete(Long tenantId, Long id) throws NotFoundException {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory not found with id: " + id));

        if (!tenantId.equals(inv.getTenantId())) {
            throw new NotFoundException("Inventory not found with id: " + id);
        }

        inventoryRepository.delete(inv);
    }

    public String importInventory(MultipartFile file, Long tenantId) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        var rows = excelParserService.read(
                file.getInputStream(),
                0,   // sheet index
                1,   // start row (skip header)
                CreateInventoryDto.class
        );
        rows.forEach(dto -> {
            try {
                create(tenantId, dto);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        return "Imported " + rows.size() + " rows successfully";
    }

    @Transactional(readOnly = true)
    public byte[] exportToExcel(Long tenantId, Long branchId, PageFilter pageFilter) {

        // Collect all matching inventory rows across pages (200 per batch)
        List<InventoryDto> items;
        {
            pageFilter.setSize(200);
            pageFilter.setPage(0);
            pageFilter.setSortBy("id");
            pageFilter.setSortDir("asc");
            Page<InventoryDto> firstPage = getAll(tenantId, branchId, pageFilter);
            items = new ArrayList<>(firstPage.getContent());
            int totalPages = firstPage.getTotalPages();
            for (int p = 1; p < totalPages; p++) {
                pageFilter.setPage(p);
                items.addAll(getAll(tenantId, branchId, pageFilter).getContent());
            }
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Inventory");

            // ---- Styles ----
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 13, (byte) 71, (byte) 161}, null));
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 33, (byte) 150, (byte) 243}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);

            XSSFCellStyle evenStyle = workbook.createCellStyle();
            evenStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 227, (byte) 242, (byte) 253}, null));
            evenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            evenStyle.setBorderBottom(BorderStyle.THIN);
            evenStyle.setBorderTop(BorderStyle.THIN);
            evenStyle.setBorderLeft(BorderStyle.THIN);
            evenStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle oddStyle = workbook.createCellStyle();
            oddStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            oddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            oddStyle.setBorderBottom(BorderStyle.THIN);
            oddStyle.setBorderTop(BorderStyle.THIN);
            oddStyle.setBorderLeft(BorderStyle.THIN);
            oddStyle.setBorderRight(BorderStyle.THIN);

            // Columns matching InventoryDto fields (product image excluded)
            String[] headers   = {"#", "ID", "Branch", "Product Code", "Product Name", "Category", "Price", "Quantity", "Created At", "Created By", "Updated At", "Updated By"};
            int[]    colWidths = { 6,   8,    20,       15,             25,             20,          12,       10,         22,            20,           22,            20};

            // Title row
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Inventory Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            // Header row
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }

            // Data rows
            int rowNum = 2;
            int seq = 1;
            for (InventoryDto inv : items) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(18);
                XSSFCellStyle rowStyle = (rowNum % 2 == 0) ? evenStyle : oddStyle;

                String branchName   = inv.branch()  != null ? nvl(inv.branch().branchName())                       : "";
                String productCode  = inv.product() != null ? nvl(inv.product().code())                            : "";
                String productName  = inv.product() != null ? nvl(inv.product().name())                            : "";
                String categoryName = inv.product() != null && inv.product().categoryDto() != null
                                      ? nvl(inv.product().categoryDto().name()) : "";
                String price        = inv.product() != null && inv.product().price() != null
                                      ? String.valueOf(inv.product().price()) : "";

                createCell(row, 0,  String.valueOf(seq++), rowStyle);
                createCell(row, 1,  inv.id() != null ? String.valueOf(inv.id()) : "", rowStyle);
                createCell(row, 2,  branchName, rowStyle);
                createCell(row, 3,  productCode, rowStyle);
                createCell(row, 4,  productName, rowStyle);
                createCell(row, 5,  categoryName, rowStyle);
                createCell(row, 6,  price, rowStyle);
                createCell(row, 7,  inv.quantity() != null ? String.valueOf(inv.quantity()) : "", rowStyle);
                createCell(row, 8,  nvl(inv.createdAt()), rowStyle);
                createCell(row, 9,  nvl(inv.createdBy()), rowStyle);
                createCell(row, 10, nvl(inv.updatedAt()), rowStyle);
                createCell(row, 11, nvl(inv.updatedBy()), rowStyle);

                rowNum++;
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private InventoryDto toDto(Inventory inv) {
        Branch branch = inv.getBranch() != null ? inv.getBranch() : null;
        Product product = inv.getProduct() != null ? inv.getProduct() : null;

        return new InventoryDto(
                inv.getId(),
                branch != null ? branchService.toResponse(branch) : null,
                product != null ? productService.toDto(product) : null,
                inv.getQuantity(),
                inv.getCreatedAt().toString(),
                inv.getCreatedBy(),
                inv.getUpdatedAt().toString(),
                inv.getUpdatedBy()
        );
    }
}

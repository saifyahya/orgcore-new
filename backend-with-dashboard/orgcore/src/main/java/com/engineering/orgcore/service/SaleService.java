package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.sales.CreateSaleDto;
import com.engineering.orgcore.dto.sales.SaleDto;
import com.engineering.orgcore.dto.sales.SaleItemDto;
import com.engineering.orgcore.entity.*;
import com.engineering.orgcore.enums.*;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.*;
import com.engineering.orgcore.util.ExcelParserService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final Utils utils;
    private final BranchService branchService;
    private final ExcelParserService excelParserService;
    private final SalePdfExportService salePdfExportService;


    public SaleDto create(Long tenantId, CreateSaleDto request) throws NotFoundException {

        if (request.branchId() == null) throw new IllegalArgumentException("branchId is required");
        if (request.items() == null || request.items().isEmpty())
            throw new IllegalArgumentException("items are required");

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found with id: " + request.branchId()));

        if (!tenantId.equals(branch.getTenantId())) {
            throw new NotFoundException("Branch not found with id: " + request.branchId());
        }

        Sale sale = new Sale();
        sale.setTenantId(tenantId);
        sale.setBranch(branch);
        sale.setDiscountRate(request.discountRate() != null ? request.discountRate() : 0.0);
        sale.setTaxRate(request.taxRate() != null ? request.taxRate() : 0.0);
        sale.setPaymentMethod(PaymentMethod.fromValue(request.paymentMethod()));
        sale.setChannel(request.channel() != null ? SaleChannel.fromValue(request.channel()) : sale.getChannel());
        sale.setExternalRef(request.externalRef());
        sale.setCreatedBy(utils.getCurrentUserName());
        sale.setCreatedAt(LocalDateTime.now());
        sale.setUpdatedBy(utils.getCurrentUserName());
        sale.setUpdatedAt(LocalDateTime.now());

        double itemsTotal = 0.0;

        // 1) Build sale items + validate & reserve stock (decrement inventory)
        for (SaleItemDto itemDto : request.items()) {

            if (itemDto.productId() == null) throw new IllegalArgumentException("productId is required");
            if (itemDto.quantity() == null || itemDto.quantity() <= 0)
                throw new IllegalArgumentException("quantity must be > 0");

            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + itemDto.productId()));

            if (!tenantId.equals(product.getTenantId())) {
                throw new NotFoundException("Product not found with id: " + itemDto.productId());
            }

            // LOCK inventory row
            Inventory inv = inventoryRepository.findForUpdate(tenantId, branch.getId(), product.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "No inventory row for productId=" + product.getId() + " in branchId=" + branch.getId()
                    ));

            long available = inv.getQuantity() != null ? inv.getQuantity() : 0L;
            int required = itemDto.quantity();

            if (available < required) {
                throw new IllegalStateException(
                        "Not enough stock for productId=" + product.getId()
                                + ". Available=" + available + ", required=" + required
                );
            }

            // decrement inventory
            inv.setQuantity(available - required);
            // no save() needed; dirty checking will persist at commit

            double unitPrice = (itemDto.unitPrice() != null)
                    ? itemDto.unitPrice()
                    : (product.getPrice() != null ? product.getPrice() : 0.0);

            double discountRate = inv.getDiscountRate() != null ? inv.getDiscountRate()/100 : 0.0;

            double lineTotal = (unitPrice - discountRate) * required;

            SaleItem saleItem = new SaleItem();
            saleItem.setTenantId(tenantId);
            saleItem.setProduct(product);
            saleItem.setQuantity(required);
            saleItem.setUnitPrice(unitPrice);
            saleItem.setLineTotal(lineTotal);

            sale.addItem(saleItem);

            itemsTotal += lineTotal;
        }

        double total = itemsTotal - (sale.getDiscountRate() /100 *itemsTotal) + (sale.getTaxRate()/100 * itemsTotal);
        if (total < 0) total = 0;
        sale.setTotalAmount(total);

        // 2) Save sale + items
        Sale saved = saleRepository.save(sale);

        // 3) Create StockMovement rows (audit)
        for (SaleItem si : saved.getItems()) {
            StockMovement sm = new StockMovement();
            sm.setTenantId(tenantId);
            sm.setBranchId(saved.getBranch().getId());
            sm.setProductId(si.getProduct().getId());
            sm.setType(StockMovementType.OUT);
            sm.setReason(StockMovementReason.SALE);
            sm.setQuantity(si.getQuantity());
            sm.setUnitCost(null);
            sm.setRefType(ReferenceType.SALE);
            sm.setRefId(String.format("SaleId-%s", saved.getExternalRef()));
            sm.setNote(null);

            stockMovementRepository.save(sm);
        }

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public SaleDto getById(Long tenantId, Long id) throws NotFoundException {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));

        if (!tenantId.equals(sale.getTenantId())) {
            throw new NotFoundException("Sale not found with id: " + id);
        }

        return toDto(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleDto> getAll(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate, PageFilter pageFilter) {
        return saleRepository.findAllByTenantId(tenantId, branchId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), pageFilter.toPageable()).map(this::toDto);
    }

    public void delete(Long tenantId, Long id) throws NotFoundException {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));

        if (!tenantId.equals(sale.getTenantId())) {
            throw new NotFoundException("Sale not found with id: " + id);
        }

        // Usually you do NOT hard delete sales in real systems.
        // But for now:
        saleRepository.delete(sale);

        // Also consider deleting related stock movements (or keep them as history)
    }


    public String importSales(MultipartFile file, Long tenantId) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        var rows = excelParserService.read(
                file.getInputStream(),
                0,   // sheet index
                1,   // start row (skip header)
                CreateSaleDto.class
        );
        rows.forEach(dto -> {
            try {
                create(tenantId, dto);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return "Imported " + rows.size() + " sales successfully";
    }

    @Transactional(readOnly = true)
    public byte[] exportSalePdf(Long tenantId, Long id) throws NotFoundException, IOException {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));

        if (!tenantId.equals(sale.getTenantId())) {
            throw new NotFoundException("Sale not found with id: " + id);
        }

        return salePdfExportService.exportSaleToPdf(sale);
    }

    @Transactional(readOnly = true)
    public byte[] exportToExcel(Long tenantId, Long branchId, LocalDate startDate, LocalDate endDate, PageFilter pageFilter) {

        // Collect all matching sales across pages
        List<SaleDto> sales;
        {
            pageFilter.setSize(200);
            pageFilter.setPage(0);
            pageFilter.setSortBy("id");
            pageFilter.setSortDir("asc");
            Page<SaleDto> firstPage = getAll(tenantId, branchId, startDate, endDate, pageFilter);
            sales = new ArrayList<>(firstPage.getContent());
            int totalPages = firstPage.getTotalPages();
            for (int p = 1; p < totalPages; p++) {
                pageFilter.setPage(p);
                sales.addAll(getAll(tenantId, branchId, startDate, endDate, pageFilter).getContent());
            }
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Sales");

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

            // Columns: #, ID, Branch, Total Amount, Discount Rate, Tax Rate, Final Amount, Payment Method, Channel, External Ref, Items, Created At, Created By, Updated At, Updated By
            String[] headers   = {"#", "ID", "Branch", "Total Amount", "Discount %", "Tax %", "Final Amount", "Payment Method", "Channel", "External Ref", "Items", "Created At", "Created By", "Updated At", "Updated By"};
            int[]    colWidths = { 6,   8,    20,       15,             12,            8,        14,             17,              15,        18,             45,       22,           20,            22,            20};

            // Title row
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Sales Report");
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
            for (SaleDto s : sales) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(18);
                XSSFCellStyle rowStyle = (rowNum % 2 == 0) ? evenStyle : oddStyle;

                // Summarise items as "Qty x Name" joined by " | "
                String itemsSummary = s.items() != null
                        ? s.items().stream()
                            .map(i -> i.quantity() + "x " + nvl(i.name()) + " (" + nvl(i.code()) + ")")
                            .collect(Collectors.joining(" | "))
                        : "";

                createSaleCell(row, 0,  String.valueOf(seq++), rowStyle);
                createSaleCell(row, 1,  s.id() != null ? String.valueOf(s.id()) : "", rowStyle);
                createSaleCell(row, 2,  s.branch() != null ? nvl(s.branch().branchName()) : "", rowStyle);
                createSaleCell(row, 3,  s.totalAmount() != null ? String.valueOf(s.totalAmount()) : "", rowStyle);
                createSaleCell(row, 4,  s.discountRate() != null ? s.discountRate() + "%" : "", rowStyle);
                createSaleCell(row, 5,  s.taxRate() != null ? s.taxRate() + "%" : "", rowStyle);
                createSaleCell(row, 6,  s.finalAmount() != null ? String.valueOf(s.finalAmount()) : "", rowStyle);
                createSaleCell(row, 7,  s.paymentMethod() != null ? s.paymentMethod().name() : "", rowStyle);
                createSaleCell(row, 8,  s.channel() != null ? s.channel().name() : "", rowStyle);
                createSaleCell(row, 9,  nvl(s.externalRef()), rowStyle);
                createSaleCell(row, 10, itemsSummary, rowStyle);
                createSaleCell(row, 11, nvl(s.createdAt()), rowStyle);
                createSaleCell(row, 12, nvl(s.createdBy()), rowStyle);
                createSaleCell(row, 13, nvl(s.updatedAt()), rowStyle);
                createSaleCell(row, 14, nvl(s.updatedBy()), rowStyle);

                rowNum++;
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void createSaleCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private SaleDto toDto(Sale sale) {
        var items = sale.getItems().stream()
                .map(i -> new SaleItemDto(i.getProduct().getId(),
                        i.getProduct().getCode(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();

        return new SaleDto(
                sale.getId(),
                branchService.toResponse(
                sale.getBranch()),
                sale.getTotalAmount(),
                sale.getDiscountRate(),
                sale.getTaxRate(),
                sale.getTotalAmount(),
                sale.getPaymentMethod(),
                sale.getChannel(),
                sale.getExternalRef(),
                items,
                sale.getCreatedAt().toString(),
                sale.getCreatedBy(),
                sale.getUpdatedAt().toString(),
                sale.getUpdatedBy()
        );
    }
}

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
import com.engineering.orgcore.util.ExcelParserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final ExcelParserService excelParserService;


    @Transactional(rollbackFor = Exception.class)
    public ProductDto create(Long tenantId, CreateProductDto request, MultipartFile imageFile) throws NotFoundException {

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.categoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        if (productRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.name().trim())) {
            throw new IllegalArgumentException("Product name already exists"+ request.name().trim());
        }

        Category category = categoryRepository.findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + request.categoryId()));

        Product product = new Product();
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setCode(request.code() != null ? request.code() : UUID.randomUUID().toString());

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile);
            product.setImage(imagePath);
        }

        product.setPrice(request.price() !=null ? request.price() : 0.0);
        product.setIsActive(request.isActive() != null ? request.isActive() : 1);
        product.setTenantId(tenantId);
        product.setCreatedBy(utils.getCurrentUserEmail());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedBy(utils.getCurrentUserEmail());
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


        product.setUpdatedBy(utils.getCurrentUserEmail());
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


    public String importProduct(MultipartFile file, Long tenantId) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        var rows = excelParserService.read(
                file.getInputStream(),
                0,   // sheet index
                1,   // start row (skip header)
                CreateProductDto.class
        );
        rows.forEach(dto -> {
            try {
                create(tenantId, dto, null);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return "Imported " + rows.size() + " products successfully";
    }

        public ProductDto toDto(Product p) {
        String encryptedImage = (p.getImage());
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCode(),
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

    @Transactional(readOnly = true)
    public byte[] exportToExcel(Long tenantId, PageFilter pageFilter) {
        // Collect all matching products across pages (max 200 per page)
        List<ProductDto> products;
        {
            pageFilter.setSize(200);
            pageFilter.setPage(0);
            pageFilter.setSortBy("id");
            pageFilter.setSortDir("asc");
            Page<ProductDto> firstPage = getAll(tenantId, pageFilter);
            products = new java.util.ArrayList<>(firstPage.getContent());
            int totalPages = firstPage.getTotalPages();
            for (int p = 1; p < totalPages; p++) {
                pageFilter.setPage(p);
                products.addAll(getAll(tenantId, pageFilter).getContent());
            }
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Products");

            // ---- Styles ----
            // Header style
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 33, (byte) 150, (byte) 243}, null)); // blue
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

            // Title style
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 13, (byte) 71, (byte) 161}, null)); // dark blue
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Even row style
            XSSFCellStyle evenStyle = workbook.createCellStyle();
            evenStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 227, (byte) 242, (byte) 253}, null));
            evenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            evenStyle.setBorderBottom(BorderStyle.THIN);
            evenStyle.setBorderTop(BorderStyle.THIN);
            evenStyle.setBorderLeft(BorderStyle.THIN);
            evenStyle.setBorderRight(BorderStyle.THIN);

            // Odd row style
            XSSFCellStyle oddStyle = workbook.createCellStyle();
            oddStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            oddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            oddStyle.setBorderBottom(BorderStyle.THIN);
            oddStyle.setBorderTop(BorderStyle.THIN);
            oddStyle.setBorderLeft(BorderStyle.THIN);
            oddStyle.setBorderRight(BorderStyle.THIN);

            // Column definitions (excluding image)
            String[] headers = {"#", "ID", "Name", "Description", "Code", "Category", "Price", "Active", "Created By", "Created At", "Updated By", "Updated At"};
            int[] colWidths  = {  8,   8,   25,     35,            15,     20,         12,       10,       20,            22,           20,           22};

            // Title row
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Products Report");
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
            for (ProductDto p : products) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(18);
                XSSFCellStyle rowStyle = (rowNum % 2 == 0) ? evenStyle : oddStyle;

                createCell(row, 0, String.valueOf(seq++), rowStyle);
                createCell(row, 1, p.id() != null ? String.valueOf(p.id()) : "", rowStyle);
                createCell(row, 2, nvl(p.name()), rowStyle);
                createCell(row, 3, nvl(p.description()), rowStyle);
                createCell(row, 4, nvl(p.code()), rowStyle);
                createCell(row, 5, p.categoryDto() != null ? nvl(p.categoryDto().name()) : "", rowStyle);
                createCell(row, 6, p.price() != null ? String.valueOf(p.price()) : "", rowStyle);
                createCell(row, 7, p.isActive() != null ? (p.isActive() == 1 ? "Active" : "Inactive") : "", rowStyle);
                createCell(row, 8, nvl(p.createdBy()), rowStyle);
                createCell(row, 9, nvl(p.createdAt()), rowStyle);
                createCell(row, 10, nvl(p.updatedBy()), rowStyle);
                createCell(row, 11, nvl(p.updatedAt()), rowStyle);

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
}

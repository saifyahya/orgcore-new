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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

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
        sale.setDiscountAmount(request.discountAmount() != null ? request.discountAmount() : 0.0);
        sale.setTaxAmount(request.taxAmount() != null ? request.taxAmount() : 0.0);
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

            double lineTotal = unitPrice * required;

            SaleItem saleItem = new SaleItem();
            saleItem.setTenantId(tenantId);
            saleItem.setProduct(product);
            saleItem.setQuantity(required);
            saleItem.setUnitPrice(unitPrice);
            saleItem.setLineTotal(lineTotal);

            sale.addItem(saleItem);

            itemsTotal += lineTotal;
        }

        double total = itemsTotal - sale.getDiscountAmount() + sale.getTaxAmount();
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
    public Page<SaleDto> getAll(Long tenantId, PageFilter pageFilter) {
        return saleRepository.findAllByTenantId(tenantId, pageFilter.toPageable()).map(this::toDto);
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
                branchService.toResponse(sale.getBranch()),
                sale.getTotalAmount(),
                sale.getDiscountAmount(),
                sale.getTaxAmount(),
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

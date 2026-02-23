package com.engineering.orgcore.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public Page<InventoryDto> getAll(Long tenantId, PageFilter pageFilter, Long branchId, Long productId) {

        var pageable = pageFilter.toPageable();
        Page<Inventory> page;

        if (branchId != null && productId != null) {
            page = inventoryRepository.findAllByTenantIdAndBranch_IdAndProduct_Id(tenantId, branchId, productId, pageable);
        } else if (branchId != null) {
            page = inventoryRepository.findAllByTenantIdAndBranch_Id(tenantId, branchId, pageable);
        } else if (productId != null) {
            page = inventoryRepository.findAllByTenantIdAndProduct_Id(tenantId, productId, pageable);
        } else {
            page = inventoryRepository.findAllByTenantId(tenantId, pageable);
        }

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

    private InventoryDto toDto(Inventory inv) {
        Branch branch = inv.getBranch() != null ? inv.getBranch() : null;
        Product product = inv.getProduct() != null ? inv.getProduct() : null;

        return new InventoryDto(
                inv.getId(),
                branch != null ? branchService.toResponse(branch) : null,
                product != null ? productService.toDto(product) : null,
                inv.getQuantity()
        );
    }
}

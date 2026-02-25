package com.engineering.orgcore.service;

import com.engineering.orgcore.config.Utils;
import com.engineering.orgcore.dto.branch.BranchDto;
import com.engineering.orgcore.dto.filter.PageFilter;
import com.engineering.orgcore.dto.product.ProductDto;
import com.engineering.orgcore.dto.sales.StockMovementDto;
import com.engineering.orgcore.entity.StockMovement;
import com.engineering.orgcore.enums.ReferenceType;
import com.engineering.orgcore.enums.StockMovementReason;
import com.engineering.orgcore.exceptions.NotFoundException;
import com.engineering.orgcore.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final Utils utils;
    private final BranchService branchService;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<StockMovementDto> getAll(Long tenantId, PageFilter pageFilter, Long branchId, Long productId){
        var pageable = pageFilter.toPageable();

        Page<StockMovement> page;
        if (branchId != null) {
            page = stockMovementRepository.findAllByTenantIdAndBranchId(tenantId, branchId, pageable);
        } else if (productId != null) {
            page = stockMovementRepository.findAllByTenantIdAndProductId(tenantId, productId, pageable);
        } else {
            page = stockMovementRepository.findAllByTenantId(tenantId, pageable);
        }

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public StockMovementDto getById(Long tenantId, Long id) throws NotFoundException {
        StockMovement sm = stockMovementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StockMovement not found with id: " + id));

        if (!tenantId.equals(sm.getTenantId())) {
            throw new NotFoundException("StockMovement not found with id: " + id);
        }

        return toDto(sm);
    }

    // Optional: manual adjustment entry (admin-only)
    public StockMovementDto createManualAdjustment(Long tenantId, StockMovementDto request) {
        if (request.branch().id() == null || request.product().id() == null) {
            throw new IllegalArgumentException("branchId and productId are required");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (request.type() == null) {
            throw new IllegalArgumentException("type is required");
        }

        StockMovement sm = new StockMovement();
        sm.setTenantId(tenantId);
        sm.setBranchId(request.branch().id());
        sm.setProductId(request.product().id());
        sm.setType(request.type()); // IN/OUT/ADJUST
        sm.setReason(request.reason() != null ? request.reason() : StockMovementReason.MANUAL);
        sm.setQuantity(request.quantity());
        sm.setUnitCost(request.unitCost());
        sm.setRefType(request.refType() != null ? request.refType() : ReferenceType.MANUAL);
        sm.setRefId(request.refId());
        sm.setNote(request.note());
        sm.setCreatedBy(utils.getCurrentUserName());
        sm.setCreatedAt(LocalDateTime.now());
        sm.setUpdatedBy(utils.getCurrentUserName());
        sm.setUpdatedAt(LocalDateTime.now());

        StockMovement saved = stockMovementRepository.save(sm);

        // If you maintain InventoryBalance, update it here too.

        return toDto(saved);
    }

    private StockMovementDto toDto(StockMovement sm) {
        try {
            BranchDto branchDto = branchService.getById(sm.getBranchId());// to validate branch exists
            ProductDto productDto = productService.getById(sm.getProductId());// to validate product exists
            return new StockMovementDto(
                    sm.getId(),
                    branchDto,
                    productDto,
                    sm.getType(),
                    sm.getReason(),
                    sm.getQuantity(),
                    sm.getUnitCost(),
                    sm.getRefType(),
                    sm.getRefId(),
                    sm.getNote(),
                    sm.getCreatedAt().toString(),
                    sm.getCreatedBy(),
                    sm.getUpdatedAt().toString(),
                    sm.getUpdatedBy()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

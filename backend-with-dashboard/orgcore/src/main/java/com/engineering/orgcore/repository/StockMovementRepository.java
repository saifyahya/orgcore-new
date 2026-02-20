package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<StockMovement> findAllByTenantIdAndBranchId(Long tenantId, Long branchId, Pageable pageable);

    Page<StockMovement> findAllByTenantIdAndProductId(Long tenantId, Long productId, Pageable pageable);

    Page<StockMovement> findAllByTenantIdAndRefTypeAndRefId(Long tenantId, String refType, UUID refId, Pageable pageable);
}

package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i from Inventory i
        where i.tenantId = :tenantId
          and i.branch.id = :branchId
          and i.product.id = :productId
    """)
    Optional<Inventory> findForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("branchId") Long branchId,
            @Param("productId") Long productId
    );


    Page<Inventory> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<Inventory> findAllByTenantIdAndBranch_Id(Long tenantId, Long branchId, Pageable pageable);

    Page<Inventory> findAllByTenantIdAndProduct_Id(Long tenantId, Long productId, Pageable pageable);

    Page<Inventory> findAllByTenantIdAndBranch_IdAndProduct_Id(Long tenantId, Long branchId, Long productId, Pageable pageable);

    Optional<Inventory> findByTenantIdAndBranch_IdAndProduct_Id(Long tenantId, Long branchId, Long productId);
}

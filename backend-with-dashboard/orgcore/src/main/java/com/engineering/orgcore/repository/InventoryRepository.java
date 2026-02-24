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

    @Query("""
         SELECT i FROM Inventory i
         WHERE i.tenantId = :tenantId
         AND ( :branchId IS NULL OR i.branch.id = :branchId)
         AND ( :q IS NULL  OR :q = '' OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', :q, '%'))
    )
    """)
    Page<Inventory> findAllByTenantId( @Param("tenantId") Long tenantId,
                                      @Param("branchId") Long branchId,
                                       @Param("q") String search,
                                       Pageable pageable);

    Optional<Inventory> findByTenantIdAndBranch_IdAndProduct_Code(Long tenantId, Long branchId, String productCode);
}

package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("""
                    SELECT SUM(si.quantity) FROM SaleItem si
                    WHERE si.tenantId = :tenantId
                    AND (si.createdAt >= :startDate AND si.createdAt < :endDate)
                    AND (:branchId IS NULL OR si.sale.branch.id = :branchId )
            """)
    Long getTotalSaleItems(Long tenantId, Long branchId, LocalDateTime startDate, LocalDateTime endDate);
}

package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
    Optional<Product> findByCode(String code);

    @Query("""
SELECT DISTINCT p FROM Product p
LEFT JOIN p.inventories inv
LEFT JOIN inv.branch b
LEFT JOIN p.category c
WHERE p.tenantId = :tenantId
AND (
      :q IS NULL
   OR :q = ''
   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
)
AND (
      :isActive IS NULL
   OR p.isActive = :isActive
)
AND (
      :categoryId IS NULL
   OR p.category.id = :categoryId
)
AND (
      :branchId IS NULL
   OR b.id = :branchId
)
""")
    Page<Product> findAllByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("q") String search,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Integer isActive,
            @Param("branchId") Long branchId,
            Pageable pageable
    );
}

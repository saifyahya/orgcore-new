package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);


    @Query("""
SELECT p FROM Product p
WHERE p.tenantId = :tenantId
AND (
      :q IS NULL
   OR :q = ''
   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
)
AND (
      :active IS NULL
   OR p.isActive = :active
)
AND (
      :categoryId IS NULL
   OR p.category.id = :categoryId
)
""")
    Page<Product> findAllByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("q") String search,
            @Param("categoryId") Long categoryId,
            @Param("active") Integer active,
            Pageable pageable
    );
}

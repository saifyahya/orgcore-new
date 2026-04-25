package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);


    @Query("""
    SELECT c FROM Category c
    WHERE c.tenantId = :tenantId
    AND (
          :q IS NULL
       OR :q = ''
       OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
    )
    AND (
          :active IS NULL
       OR c.isActive = :active
    )
    """)
    Page<Category> findAllByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("q") String search,
            @Param("active") Integer active,
            Pageable pageable
    );

    Long countByTenantId(Long tenantId);

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);
}

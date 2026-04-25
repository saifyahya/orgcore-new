package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    boolean existsByBranchNameIgnoreCaseAndTenantId(String branchName, Long tenantId);
    @Query("""
SELECT b FROM Branch b
WHERE b.tenantId = :tenantId
AND (
      :q IS NULL
   OR :q = ''
   OR LOWER(b.branchName) LIKE LOWER(CONCAT('%', :q, '%'))
   OR LOWER(b.address) LIKE LOWER(CONCAT('%', :q, '%'))
)
AND (
      :active IS NULL
   OR b.isActive = :active
)
""")
    Page<Branch> findAllByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("q") String search,
            @Param("active") Integer active,
            Pageable pageable
    );

    long countByTenantIdAndIsActive(Long tenantId, Integer isActive);

}
package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    boolean existsByTenantNameAndIsActive(String tenantName, Integer isActive);
    boolean existsByEmailAndIsActive(String email, Integer isActive);
    boolean existsByPhoneAndIsActive(String phone, Integer isActive);
    Optional<Tenant> findByTenantName(String name);
}

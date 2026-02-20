package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}

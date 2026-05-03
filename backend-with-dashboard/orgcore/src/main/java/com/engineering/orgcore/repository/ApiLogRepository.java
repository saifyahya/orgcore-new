package com.engineering.orgcore.repository;

import com.engineering.orgcore.entity.ApiLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiLogRepository extends JpaRepository<ApiLogEntity,Long> {
}
